// package com.platform.SkyMaster_Hub.config;

// import org.junit.jupiter.api.Test;
// import static org.junit.jupiter.api.Assertions.*;

// class CustomCacheTest {

//     @Test
//     void testPutAndGet() {
//         CustomCache<String, String> cache = new CustomCache<>(1000L); // 1s TTL
//         cache.put("key1", "value1");

//         assertEquals("value1", cache.get("key1"));
//     }

//     @Test
//     void testExpiration() throws InterruptedException {
//         CustomCache<String, String> cache = new CustomCache<>(100L); // 0.1s TTL
//         cache.put("key2", "value2");

//         Thread.sleep(150); // chờ cache hết hạn
//         assertNull(cache.get("key2"));
//     }

//     @Test
//     void testRemove() {
//         CustomCache<String, String> cache = new CustomCache<>(1000L);
//         cache.put("key3", "value3");
//         cache.remove("key3");

//         assertNull(cache.get("key3"));
//     }
// }
