package com.platform.SkyMaster_Hub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin endpoints for operational tasks. Exposes cache management helpers.
 * Note: In production, protect these endpoints with proper authentication/authorization.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final CacheManager cacheManager;

    @Autowired
    public AdminController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Clear caches managed by Spring Cache.
     * - If no name is provided or name=ALL, clears all caches.
     * - If a specific cache name is provided, clears only that cache.
     * Examples:
     *   POST /admin/cache/clear
     *   POST /admin/cache/clear?name=yourCacheName
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearCache(@RequestParam(value = "name", required = false) String name) {
        Map<String, Object> resp = new HashMap<>();
        try {
            if (name == null || name.equalsIgnoreCase("ALL")) {
                for (String cacheName : cacheManager.getCacheNames()) {
                    var c = cacheManager.getCache(cacheName);
                    if (c != null) {
                        c.clear();
                    }
                }
                resp.put("status", "cleared_all_caches");
            } else {
                var c = cacheManager.getCache(name);
                if (c != null) {
                    c.clear();
                    resp.put("status", "cleared_" + name);
                } else {
                    resp.put("status", "no_such_cache");
                    resp.put("cacheName", name);
                    return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
                }
            }
            resp.put("timestamp", System.currentTimeMillis());
            resp.put("cacheNames", cacheManager.getCacheNames());
            return new ResponseEntity<>(resp, HttpStatus.OK);
        } catch (Exception e) {
            resp.put("error", e.getMessage());
            return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Basic health endpoint for admin checks.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> m = new HashMap<>();
        m.put("status", "UP");
        m.put("cacheCount", cacheManager.getCacheNames().size());
        m.put("timestamp", System.currentTimeMillis());
        return new ResponseEntity<>(m, HttpStatus.OK);
    }
}
