package com.platform.SkyMaster_Hub.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.SkyMaster_Hub.config.CustomCache;
import com.platform.SkyMaster_Hub.config.LruCache;
import com.platform.SkyMaster_Hub.dto.FlightScheduleDTO;
import com.platform.SkyMaster_Hub.dto.response.AirLabResponse;
import com.platform.SkyMaster_Hub.entity.FlightSchedules;
import com.platform.SkyMaster_Hub.exception.AirLabApiException;
import com.platform.SkyMaster_Hub.mapper.FlightScheduleMapper;
import com.platform.SkyMaster_Hub.repository.FlightSchedulesRepository;

@Service
public class FlightScheduleService {

    private final WebClient.Builder webClientBuilder;
    private final FlightSchedulesRepository schedulesRepository;
    private final FlightScheduleMapper scheduleMapper;
    private final CustomCache<String, List<FlightSchedules>> schedulesCache;
    LruCache<String, List<FlightScheduleDTO>> cacheByDepIata;
    private final Map<String, CompletableFuture<List<FlightScheduleDTO>>> inFlight = new HashMap<>();

    private final Object lock = new Object();

    // Executor dùng chung (KHÔNG tạo mới mỗi request)
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    @Value("${airlab.api.key}")
    private String apiKey;
    @Value("${airlab.api.base-url}")
    private String baseUrl;

    public FlightScheduleService(Builder webClientBuilder, FlightSchedulesRepository flightschedulesrepository,
            FlightScheduleMapper flightScheduleMapper) {
        this.webClientBuilder = webClientBuilder;
        this.schedulesRepository = flightschedulesrepository;
        this.scheduleMapper = flightScheduleMapper;
        this.cacheByDepIata = new LruCache<>(3,10 * 60 * 1000);
        this.schedulesCache = new CustomCache<>(10 * 60 * 1000L, 100);
    }
    public List<FlightSchedules> fetchAndSaveFlightSchedules(String depIata) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();
            String rawResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path("/schedules")
                    .queryParam("api_key", apiKey)
                    .queryParam("dep_iata", depIata)
                    .queryParam("_fields", "flight_iata,flight_icao,airline_iata,duration,dep_iata,dep_icao,arr_iata,arr_icao,dep_time,arr_time")
                    .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (rawResponse == null || rawResponse.isEmpty()) {
                throw new AirLabApiException("No data received from AirLab API");
            }

            AirLabResponse<FlightScheduleDTO> response;
            try {
                ObjectMapper mapper = new ObjectMapper();
                response = mapper.readValue(rawResponse, new TypeReference<AirLabResponse<FlightScheduleDTO>>() {
                });
            } catch (Exception parseException) {
                throw new AirLabApiException("Failed to parse API response: " + parseException.getMessage());
            }

            // Chỉ ném khi có error message thực sự
            if (response.getError() != null && response.getError().getMessage() != null) {
                throw new AirLabApiException("AirLab API error: " + response.getError().getMessage());
            }

            // Ném khi response rỗng
            if (response.getResponse() == null || response.getResponse().isEmpty()) {
                throw new AirLabApiException("No data received from AirLab API");
            }

            // Convert all DTOs to entities (no filtering by depIata)
            List<FlightSchedules> flightSchedules = response.getResponse().stream()
                    .filter(dto -> dto.getFlightIata() != null && !dto.getFlightIata().isEmpty())
                    .map(scheduleMapper::toEntity)
                    .collect(Collectors.toList());

            // Update existing flights or create new ones
            // Use flightIata + depIata + arrIata + depTime as composite key to detect duplicates
            List<FlightSchedules> savedSchedules = flightSchedules.stream()
                    .map(schedule -> {
                        // Find by flight_iata and dep_iata and arr_iata to update existing flight schedule
                        Optional<FlightSchedules> existing = schedulesRepository.findByFlightIataAndDepIataAndArrIata(
                                schedule.getFlightIata(),
                                schedule.getDepIata(),
                                schedule.getArrIata());
                        if (existing.isPresent()) {
                            FlightSchedules existingSchedule = existing.get();
                            existingSchedule.setFlightIata(schedule.getFlightIata());
                            existingSchedule.setFlightIcao(schedule.getFlightIcao());
                            existingSchedule.setDepIata(schedule.getDepIata());
                            existingSchedule.setDepIcao(schedule.getDepIcao());
                            existingSchedule.setArrIata(schedule.getArrIata());
                            existingSchedule.setArrIcao(schedule.getArrIcao());
                            existingSchedule.setAirlineIata(schedule.getAirlineIata());
                            existingSchedule.setDepTime(schedule.getDepTime());
                            existingSchedule.setArrTime(schedule.getArrTime());
                            existingSchedule.setDuration(schedule.getDuration());
                            existingSchedule.setFetchAt(schedule.getFetchAt());
                            return schedulesRepository.save(existingSchedule);
                        } else {
                            return schedulesRepository.save(schedule);
                        }
                    })
                    .collect(Collectors.toList());

            return savedSchedules;

        } catch (WebClientResponseException e) {
            throw new AirLabApiException("AirLab API returned HTTP " + e.getRawStatusCode() + ": " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AirLabApiException("Failed to fetch schedules from AirLab API", e);
        }
    }
    //request coalescing + cacheLRU
    public List<FlightScheduleDTO> getSchedulesByDepartureAirport(String depIata) {

    String cacheKey = "dep:" + depIata;

    // 1. Cache nhanh
    List<FlightScheduleDTO> cached = cacheByDepIata.get(cacheKey);
    if (cached != null) {
        cacheByDepIata.printAll();
        return cached;
    }

    CompletableFuture<List<FlightScheduleDTO>> future;

    synchronized (lock) {
        future = inFlight.get(cacheKey);

        if (future == null) {
            future = new CompletableFuture<>();
            inFlight.put(cacheKey, future);

            CompletableFuture<List<FlightScheduleDTO>> leader = future;

            executor.submit(() -> {
                try {
                    List<FlightScheduleDTO> result =
                            loadFromDbAndApi(depIata, cacheKey);

                    cacheByDepIata.put(cacheKey, result);
                    leader.complete(result);

                } catch (Exception e) {
                    leader.completeExceptionally(e);
                } finally {
                    synchronized (lock) {
                        inFlight.remove(cacheKey);
                    }
                }
            });
        }
    }

    // follower thread chờ kết quả
    return future.join();
}
private List<FlightScheduleDTO> loadFromDbAndApi(String depIata, String cacheKey) {

    // 2. DB
    List<FlightSchedules> schedulesFromDb =
            schedulesRepository.findAllByDepIata(depIata);

    if (!schedulesFromDb.isEmpty()) {
        return schedulesFromDb.stream()
                .map(scheduleMapper::toDTO)
                .collect(Collectors.toList());
    }

    // 3. API (GIỮ NGUYÊN CODE CỦA BẠN)
    try {
        WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

        String rawResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/schedules")
                        .queryParam("api_key", apiKey)
                        .queryParam("dep_iata", depIata)
                        .queryParam("_fields",
                                "flight_iata,flight_icao,airline_iata,duration," +
                                "dep_iata,dep_icao,arr_iata,arr_icao,dep_time,arr_time")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper mapper = new ObjectMapper();
        AirLabResponse<FlightScheduleDTO> response =
                mapper.readValue(rawResponse,
                        new TypeReference<>() {});

        Set<String> seen = new HashSet<>();

        List<FlightScheduleDTO> dtoList = response.getResponse().stream()
                .filter(dto -> dto.getFlightIata() != null && !dto.getFlightIata().isBlank())
                .filter(dto -> seen.add(
                        dto.getFlightIata() + "|" + dto.getDepIata() + "|" + dto.getArrIata()
                ))
                .collect(Collectors.toList());

        // save/update DB
        dtoList.forEach(dto ->
                schedulesRepository.save(scheduleMapper.toEntity(dto))
        );

        return dtoList;

    } catch (Exception e) {
        throw new AirLabApiException("Failed to fetch schedules from AirLab API", e);
    }
}

    public List<FlightScheduleDTO> getSchedulesByArrivalAirport(String arrIata) {

    String cacheKey = "arr:" + arrIata;

    // 1. cache nhanh
    List<FlightScheduleDTO> cached = cacheByDepIata.get(cacheKey);
    if (cached != null) {
        cacheByDepIata.printAll();
        return cached;
    }

    CompletableFuture<List<FlightScheduleDTO>> future;

    synchronized (lock) {
        future = inFlight.get(cacheKey);

        if (future == null) {
            future = new CompletableFuture<>();
            inFlight.put(cacheKey, future);

            CompletableFuture<List<FlightScheduleDTO>> leader = future;

            executor.submit(() -> {
                try {
                    List<FlightScheduleDTO> result =
                            loadArrFromDbAndApi(arrIata);

                    cacheByDepIata.put(cacheKey, result);
                    leader.complete(result);

                } catch (Exception e) {
                    leader.completeExceptionally(e);
                } finally {
                    synchronized (lock) {
                        inFlight.remove(cacheKey);
                    }
                }
            });
        }
    }

    // follower threads đợi kết quả
    return future.join();
}
private List<FlightScheduleDTO> loadArrFromDbAndApi(String arrIata) {

    // 2. DB
    List<FlightSchedules> schedulesFromDb =
            schedulesRepository.findAllByArrIata(arrIata);

    if (!schedulesFromDb.isEmpty()) {
        return schedulesFromDb.stream()
                .map(scheduleMapper::toDTO)
                .collect(Collectors.toList());
    }

    // 3. API
    try {
        WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

        String rawResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/schedules")
                        .queryParam("api_key", apiKey)
                        .queryParam("arr_iata", arrIata)
                        .queryParam("_fields",
                                "flight_iata,flight_icao,airline_iata,duration," +
                                "dep_iata,dep_icao,arr_iata,arr_icao,dep_time,arr_time")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper mapper = new ObjectMapper();
        AirLabResponse<FlightScheduleDTO> response =
                mapper.readValue(rawResponse, new TypeReference<>() {});

        Set<String> seen = new HashSet<>();

        List<FlightScheduleDTO> dtoList = response.getResponse().stream()
                .filter(dto -> dto.getFlightIata() != null && !dto.getFlightIata().isBlank())
                .filter(dto -> seen.add(
                        dto.getFlightIata() + "|" + dto.getDepIata() + "|" + dto.getArrIata()
                ))
                .collect(Collectors.toList());

        // save/update DB
        dtoList.forEach(dto ->
                schedulesRepository.save(scheduleMapper.toEntity(dto))
        );

        return dtoList;

    } catch (Exception e) {
        throw new AirLabApiException("Failed to fetch schedules from AirLab API", e);
    }
}

}
