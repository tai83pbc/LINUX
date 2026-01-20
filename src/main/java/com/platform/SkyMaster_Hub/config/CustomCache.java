package com.platform.SkyMaster_Hub.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CustomCache<K, V> {

    private final Map<K, V> cache;
    private final int maxSize;
    private final ConcurrentHashMap<K, Long> expirationtimes = new ConcurrentHashMap<>();
    private final Long ttl;
    private final ScheduledExecutorService executorService;

    public CustomCache(Long ttlInMillis, int maxSize) {
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.maxSize = maxSize;
        this.ttl = ttlInMillis;
        this.cache = Collections.synchronizedMap(
                new LinkedHashMap<K, V>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > CustomCache.this.maxSize;
            }
        }
        );
    }

    public void put(K Key, V Value) {
        cache.put(Key, Value);
        expirationtimes.put(Key, System.currentTimeMillis() + ttl);
    }

    public V get(K Key) {
        if (isExpired(Key)) {
            cache.remove(Key);
            expirationtimes.remove(Key);
            return null;
        }
        return cache.get(Key);
    }

    public void remove(K key) {
        cache.remove(key);
        expirationtimes.remove(key);
    }

    public boolean isExpired(K Key) {
        Long expirationTime = expirationtimes.get(Key);
        return expirationTime == null || System.currentTimeMillis() > expirationTime;
    }

    public void startCleanupTask() {
        executorService.scheduleAtFixedRate(() -> {
            synchronized (cache) {
                for (K key : cache.keySet()) {
                    if (isExpired(key)) {
                        remove(key);
                    }
                }
            }

        }, ttl, ttl, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
