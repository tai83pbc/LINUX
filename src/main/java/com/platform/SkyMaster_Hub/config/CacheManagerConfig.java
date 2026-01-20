package com.platform.SkyMaster_Hub.config;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Callable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Cache Manager Configuration Hỗ trợ 2 chế độ: Simple LRU (in-memory) và Redis
 * (distributed)
 *
 * Kỹ thuật phân phối cache: 1. TTL (Time To Live) - Cache hết hạn sau thời gian
 * nhất định 2. LRU (Least Recently Used) - Xoá cache ít được dùng khi hết bộ
 * nhớ 3. Selective Caching - Chỉ cache dữ liệu cần thiết 4. Cache Invalidation
 * - Xoá cache khi dữ liệu thay đổi
 */
@Configuration
@EnableCaching
public class CacheManagerConfig {

    /**
     * Custom LRU Cache Manager (in-memory với LRU) Sử dụng khi:
     * spring.cache.type=simple
     *
     * TTL: 10 phút LRU: Tối đa 500 entries, xoá cái ít được dùng nhất Phù hợp
     * cho: Development, testing, single instance
     */
    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "simple")
    public CacheManager simpleLRUCacheManager() {
        return new SimpleLRUCacheManager(500, Duration.ofMinutes(10).toMillis());
    }

    /**
     * Redis Cache Manager (distributed) Sử dụng khi: spring.cache.type=redis
     *
     * TTL: Khác nhau tùy theo loại data LRU: Tự động quản lý bởi Redis
     * maxmemory policy Phù hợp cho: Production, multiple instances
     */
    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // Cấu hình mặc định - 10 phút
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                // Flight Schedules - 10 phút (thay đổi thường xuyên)
                .withCacheConfiguration("schedulesByDeparture",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration("schedulesByArrival",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(10)))
                // Airports - 30 phút
                .withCacheConfiguration("airports",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30)))
                // Airlines - 30 phút
                .withCacheConfiguration("airlines",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30)))
                // Cities - 30 phút
                .withCacheConfiguration("cities",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30)))
                // Countries - 1 ngày (hầu như không thay đổi)
                .withCacheConfiguration("countries",
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(24)))
                .build();
    }

    /**
     * Custom Cache Manager với LRU Sử dụng LinkedHashMap access-order cho LRU
     * eviction
     */
    private static class SimpleLRUCacheManager implements CacheManager {

        private final Map<String, Cache> caches = new ConcurrentHashMap<>();
        private final int maxSize;
        private final long ttlMs;

        public SimpleLRUCacheManager(int maxSize, long ttlMs) {
            this.maxSize = maxSize;
            this.ttlMs = ttlMs;
        }

        @Override
        public Cache getCache(String name) {
            return caches.computeIfAbsent(name, n -> {
                LRUCache cache = new LRUCache(n, maxSize, ttlMs);
                System.out.println("✓ Created LRU cache: " + n + " (max: " + maxSize + " entries, TTL: " + (ttlMs / 60000) + "m)");
                return cache;
            });
        }

        @Override
        public Collection<String> getCacheNames() {
            return caches.keySet();
        }
    }

    /**
     * LRU Cache Implementation using LinkedHashMap
     */
    private static class LRUCache implements Cache {

        private final String name;
        private final long ttlMs;
        private final Map<Object, Object> store;
        private final Map<Object, Long> timestamps = new ConcurrentHashMap<>();

        public LRUCache(String name, int maxSize, long ttlMs) {
            this.name = name;
            this.ttlMs = ttlMs;
            // LinkedHashMap với access-order = true cho LRU eviction
            this.store = Collections.synchronizedMap(
                    new LinkedHashMap<Object, Object>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > maxSize;
                }
            }
            );
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object getNativeCache() {
            return store;
        }

        @Override
        public ValueWrapper get(Object key) {
            // Kiểm tra TTL
            Long timestamp = timestamps.get(key);
            if (timestamp != null && System.currentTimeMillis() - timestamp > ttlMs) {
                evict(key);
                timestamps.remove(key);
                return null;
            }
            Object value = store.get(key);
            return value != null ? new SimpleValueWrapper(value) : null;
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            ValueWrapper wrapper = get(key);
            return wrapper != null ? (T) wrapper.get() : null;
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            ValueWrapper wrapper = get(key);
            if (wrapper != null) {
                return (T) wrapper.get();
            }
            try {
                T value = valueLoader.call();
                put(key, value);
                return value;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void put(Object key, Object value) {
            store.put(key, value);
            timestamps.put(key, System.currentTimeMillis());
            System.out.println(key + "    " + value);
        }

        @Override
        public ValueWrapper putIfAbsent(Object key, Object value) {
            Object existing = store.putIfAbsent(key, value);
            if (existing == null) {
                timestamps.put(key, System.currentTimeMillis());
            }
            return existing != null ? new SimpleValueWrapper(existing) : null;
        }

        @Override
        public void evict(Object key) {
            store.remove(key);
            timestamps.remove(key);
        }

        @Override
        public void clear() {
            store.clear();
            timestamps.clear();
        }

        private static class SimpleValueWrapper implements ValueWrapper {

            private final Object value;

            public SimpleValueWrapper(Object value) {
                this.value = value;
            }

            @Override
            public Object get() {
                return value;
            }
        }
    }
}
