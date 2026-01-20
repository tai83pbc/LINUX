package com.platform.SkyMaster_Hub.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * Cache Service - Quản lý vô hiệu hoá cache
 *
 * Kỹ thuật: Cache Invalidation Xoá cache khi dữ liệu thay đổi để đảm bảo dữ
 * liệu luôn mới
 */
@Service
public class CacheService {

    /**
     * Xoá tất cả flight schedule cache khi có dữ liệu mới Được gọi sau khi
     * fetchAndSaveFlightSchedules hoàn thành
     */
    @CacheEvict(value = {"schedulesByDeparture", "schedulesByArrival"}, allEntries = true)
    public void clearFlightScheduleCache() {
        System.out.println("✓ Cleared all flight schedule cache");
    }

    /**
     * Xoá cache schedules theo departure airport
     */
    @CacheEvict(value = "schedulesByDeparture", allEntries = true)
    public void clearSchedulesByDepartureCache() {
        System.out.println("✓ Cleared schedules by departure cache");
    }

    /**
     * Xoá cache schedules theo arrival airport
     */
    @CacheEvict(value = "schedulesByArrival", allEntries = true)
    public void clearSchedulesByArrivalCache() {
        System.out.println("✓ Cleared schedules by arrival cache");
    }

    /**
     * Xoá cache airport khi có dữ liệu mới
     */
    @CacheEvict(value = "airports", allEntries = true)
    public void clearAirportsCache() {
        System.out.println("✓ Cleared airports cache");
    }

    /**
     * Xoá cache airline khi có dữ liệu mới
     */
    @CacheEvict(value = "airlines", allEntries = true)
    public void clearAirlinesCache() {
        System.out.println("✓ Cleared airlines cache");
    }

    /**
     * Xoá cache city khi có dữ liệu mới
     */
    @CacheEvict(value = "cities", allEntries = true)
    public void clearCitiesCache() {
        System.out.println("✓ Cleared cities cache");
    }

    /**
     * Xoá cache country khi có dữ liệu mới
     */
    @CacheEvict(value = "countries", allEntries = true)
    public void clearCountriesCache() {
        System.out.println("✓ Cleared countries cache");
    }

    /**
     * Xoá tất cả cache
     */
    @CacheEvict(value = {"schedulesByDeparture", "schedulesByArrival", "airports", "airlines", "cities", "countries"},
            allEntries = true)
    public void clearAllCache() {
        System.out.println("✓ Cleared all cache");
    }

}
