package com.platform.SkyMaster_Hub.config;

import java.util.HashMap;
import java.util.Map;

public class LruCache<K, V> {

    private class Node {
        K key;
        V value;
        long expireAt;
        Node previous;
        Node next;

        Node(K key, V value, long expireAt) {
            this.key = key;
            this.value = value;
            this.expireAt = expireAt;
        }
    }

    private final Map<K, Node> cache = new HashMap<>();
    private final int capacity;
    private final long ttlMillis;

    private Node head; // MRU
    private Node end;  // LRU

    public LruCache(int capacity, long ttlMillis) {
        this.capacity = capacity;
        this.ttlMillis = ttlMillis;
    }

    /* ================= PUT ================= */

    public synchronized void put(K key, V value) {
        long expireAt = System.currentTimeMillis() + ttlMillis;
        Node node = cache.get(key);

        if (node != null) {
            node.value = value;
            node.expireAt = expireAt;
            moveToHead(node);
            return;
        }

        if (cache.size() >= capacity) {
            evictLRU();
        }

        Node newNode = new Node(key, value, expireAt);
        setHead(newNode);
        cache.put(key, newNode);
    }
    public synchronized void printAll() {
    System.out.println("===== LRU Cache (MRU -> LRU) =====");

    if (head == null) {
        System.out.println("[EMPTY]");
        return;
    }

    Node current = head;
    long now = System.currentTimeMillis();

    while (current != null) {
        long remaining = current.expireAt - now;

        String status;
        if (remaining <= 0) {
            status = "EXPIRED";
        } else {
            status = "TTL=" + remaining + "ms";
        }

        System.out.println(
                "Key=" + current.key +
                " | " + status +
                " | ValueType=" +
                (current.value != null ? current.value.getClass().getSimpleName() : "null")
        );

        current = current.next;
    }

    System.out.println("=================================");
}

    /* ================= GET ================= */

    public synchronized V get(K key) {
        Node node = cache.get(key);
        if (node == null) return null;

        if (System.currentTimeMillis() > node.expireAt) {
            removeNode(node);
            cache.remove(key);
            return null;
        }

        moveToHead(node);
        return node.value;
    }

    /* ================= INTERNAL ================= */

    private void evictLRU() {
        if (end != null) {
            cache.remove(end.key);
            removeNode(end);
        }
    }

    private void moveToHead(Node node) {
        removeNode(node);
        setHead(node);
    }

    private void removeNode(Node node) {
        if (node.previous != null) {
            node.previous.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.previous = node.previous;
        } else {
            end = node.previous;
        }
    }

    private void setHead(Node node) {
        node.previous = null;
        node.next = head;

        if (head != null) {
            head.previous = node;
        }

        head = node;

        if (end == null) {
            end = head;
        }
    }

    /* ================= UTIL ================= */

    public synchronized void invalidate(K key) {
        Node node = cache.remove(key);
        if (node != null) {
            removeNode(node);
        }
    }

    public synchronized void clear() {
        cache.clear();
        head = null;
        end = null;
    }

    public synchronized int size() {
        return cache.size();
    }
}
