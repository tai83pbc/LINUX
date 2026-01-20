package com.platform.SkyMaster_Hub.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.platform.SkyMaster_Hub.dto.FlightScheduleDTO;
import com.platform.SkyMaster_Hub.entity.Airlines;
import com.platform.SkyMaster_Hub.entity.Airports;
import com.platform.SkyMaster_Hub.entity.Cities;
import com.platform.SkyMaster_Hub.entity.Countries;
import com.platform.SkyMaster_Hub.entity.FlightSchedules;
import com.platform.SkyMaster_Hub.service.AirLabService;
import com.platform.SkyMaster_Hub.service.FlightScheduleService;

@RestController
@RequestMapping("/api/airlab")
public class AirLabController {

    private final AirLabService airLabService;
    private final FlightScheduleService flightScheduleService;

    public AirLabController(AirLabService airLabService,FlightScheduleService flightScheduleService) {
        this.airLabService = airLabService;
        this.flightScheduleService = flightScheduleService;
    }

    @GetMapping("/fetch/airports")
    public ResponseEntity<Map<String, Object>> fetchAirports() {
        try {
            List<Airports> airports = airLabService.fetchAndSaveAirports();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully fetched and saved airports");
            response.put("count", airports.size());
            response.put("data", airports);

            return ResponseEntity.ok(response);
        } catch (Exception e) {

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch airports from AirLab API");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            errorResponse.put("status", 400);

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping(value = "/fetch/airports", params = "city_code")
    ResponseEntity<Map<String, Object>> fetchAirportsByCityCode(@RequestParam("city_code") String cityCode) {
        try {
            List<Airports> airports = airLabService.fetchAndSaveAirportsByCityCode(cityCode);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully fetched and saved airports");
            response.put("count", airports.size());
            response.put("data", airports);

            return ResponseEntity.ok(response);
        } catch (Exception e) {

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch airports from AirLab API");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            errorResponse.put("status", 400);

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/fetch/airlines")
    public ResponseEntity<Map<String, Object>> fetchAirlines() {

        try {
            List<Airlines> airlines = airLabService.fetchAndSaveAirlines();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully fetched and saved airlines");
            response.put("count", airlines.size());
            response.put("data", airlines);

            return ResponseEntity.ok(response);
        } catch (Exception e) {

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch airlines from AirLab API");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            errorResponse.put("status", 400);

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get all airports from database GET /api/airlab/airports
     */
    @GetMapping("/airports")
    public ResponseEntity<List<Airports>> getAllAirports() {

        List<Airports> airports = airLabService.getAllAirports();
        return ResponseEntity.ok(airports);
    }

    /**
     * Get all airlines from database GET /api/airlab/airlines
     */
    @GetMapping("/airlines")
    public ResponseEntity<List<Airlines>> getAllAirlines() {

        List<Airlines> airlines = airLabService.getAllAirlines();
        return ResponseEntity.ok(airlines);
    }

    /**
     * Get airport by ICAO code GET /api/airlab/airports/{icaoCode}
     */
    @GetMapping("/airports/{icaoCode}")
    public ResponseEntity<Airports> getAirportByIcaoCode(@PathVariable String icaoCode) {

        Airports airport = airLabService.getAirportByIcaoCode(icaoCode);
        return ResponseEntity.ok(airport);
    }

    /**
     * Get airline by IATA code GET /api/airlab/airlines/{iataCode}
     */
    @GetMapping("/airlines/{iataCode}")
    public ResponseEntity<Airlines> getAirlineByIataCode(@PathVariable String iataCode) {

        Airlines airline = airLabService.getAirlineByIataCode(iataCode);
        return ResponseEntity.ok(airline);
    }

    // ============= COUNTRIES ENDPOINTS =============
    /**
     * Fetch and save countries from AirLab API GET /api/airlab/fetch/countries
     */
    @GetMapping("/fetch/countries")
    public ResponseEntity<Map<String, Object>> fetchCountries() {
        try {
            List<Countries> countries = airLabService.fetchAndSaveCountries();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully fetched and saved countries");
            response.put("count", countries.size());
            response.put("data", countries);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch countries from AirLab API");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            errorResponse.put("status", 400);

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get all countries from database GET /api/airlab/countries
     */
    @GetMapping("/countries")
    public ResponseEntity<List<Countries>> getAllCountries() {
        List<Countries> countries = airLabService.getAllCountries();
        return ResponseEntity.ok(countries);
    }

    /**
     * Get country by code GET /api/airlab/countries/{code}
     */
    @GetMapping("/countries/{code}")
    public ResponseEntity<Countries> getCountryByCode(@PathVariable String code) {
        Countries country = airLabService.getCountryByCode(code);
        return ResponseEntity.ok(country);
    }

    // ============= CITIES ENDPOINTS =============
    /**
     * Fetch and save cities from AirLab API GET /api/airlab/fetch/cities
     */
    @GetMapping("/fetch/cities")
    public ResponseEntity<Map<String, Object>> fetchCities() {
        try {
            List<Cities> cities = airLabService.fetchAndSaveCities();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully fetched and saved cities");
            response.put("count", cities.size());
            response.put("data", cities);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch cities from AirLab API");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            errorResponse.put("status", 400);

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get all cities from database GET /api/airlab/cities
     */
    @GetMapping("/cities")
    public ResponseEntity<List<Cities>> getAllCities() {
        List<Cities> cities = airLabService.getAllCities();
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/fetch/schedules")
    public ResponseEntity<Map<String, Object>> fetchSchedules(@RequestParam String depIata) {
        try {
            List<FlightSchedules> flightScheduleses = flightScheduleService.fetchAndSaveFlightSchedules(depIata);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully fetched and saved schedules");
            response.put("count", flightScheduleses.size());
            response.put("data", flightScheduleses);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch cities from AirLab API");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            errorResponse.put("status", 400);

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ============= AIRPORT SEARCH ENDPOINTS =============
    /**
     * Get all cities by country code GET
     * /api/airlab/cities/by-country/{countryCode}
     */
    @GetMapping("/cities/by-country/{countryCode}")
    public ResponseEntity<Map<String, Object>> getCitiesByCountryCode(@PathVariable String countryCode) {
        try {
            List<Cities> cities = airLabService.getCitiesByCountryCode(countryCode);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully retrieved cities");
            response.put("count", cities.size());
            response.put("data", cities);
            response.put("countryCode", countryCode);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to retrieve cities");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            errorResponse.put("status", 400);

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get flight schedules for departure airport GET
     * /api/airlab/schedules/departure/{depIata}
     */
    @GetMapping("/schedules/departure/{depIata}")
    public ResponseEntity<Map<String, Object>> getSchedulesByDepartureAirport(@PathVariable String depIata) {
        try {
            List<FlightScheduleDTO> schedules = flightScheduleService.getSchedulesByDepartureAirport(depIata);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully retrieved schedules");
            response.put("count", schedules.size());
            response.put("data", schedules);
            response.put("depIata", depIata);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to retrieve schedules");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            errorResponse.put("status", 400);

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get flight schedules for arrival airport GET
     * /api/airlab/schedules/arrival/{arrIata}
     */
    @GetMapping("/schedules/arrival/{arrIata}")
    public ResponseEntity<Map<String, Object>> getSchedulesByArrivalAirport(@PathVariable String arrIata) {
        try {
            List<FlightScheduleDTO> schedules = flightScheduleService.getSchedulesByArrivalAirport(arrIata);
            // System.out.println("list cache"); 
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully retrieved schedules");
            response.put("count", schedules.size());
            response.put("data", schedules);
            response.put("arrIata", arrIata);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to retrieve schedules");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            errorResponse.put("status", 400);

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get airports by country code GET
     * /api/airlab/airports/by-country/{countryCode}
     */
    @GetMapping("/airports/by-country/{countryCode}")
    public ResponseEntity<Map<String, Object>> getAirportsByCountryCode(@PathVariable String countryCode) {
        try {
            List<Airports> airports = airLabService.getAirportsByCountryCode(countryCode);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully retrieved airports");
            response.put("count", airports.size());
            response.put("data", airports);
            response.put("countryCode", countryCode);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to retrieve airports");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            errorResponse.put("status", 400);

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
