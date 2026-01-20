package com.platform.SkyMaster_Hub.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.platform.SkyMaster_Hub.dto.AirlineDTO;
import com.platform.SkyMaster_Hub.dto.AirportDTO;
import com.platform.SkyMaster_Hub.dto.CityDTO;
import com.platform.SkyMaster_Hub.dto.CountryDTO;
import com.platform.SkyMaster_Hub.dto.response.AirLabResponse;
import com.platform.SkyMaster_Hub.entity.Airlines;
import com.platform.SkyMaster_Hub.entity.Airports;
import com.platform.SkyMaster_Hub.entity.Cities;
import com.platform.SkyMaster_Hub.entity.Countries;
import com.platform.SkyMaster_Hub.exception.AirLabApiException;
import com.platform.SkyMaster_Hub.mapper.AirlineMapper;
import com.platform.SkyMaster_Hub.mapper.AirportMapper;
import com.platform.SkyMaster_Hub.mapper.CityMapper;
import com.platform.SkyMaster_Hub.mapper.CountryMapper;
import com.platform.SkyMaster_Hub.repository.AirlinesRepository;
import com.platform.SkyMaster_Hub.repository.AirportsRepository;
import com.platform.SkyMaster_Hub.repository.CitiesRepository;
import com.platform.SkyMaster_Hub.repository.CountriesRepository;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hibernate.annotations.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.platform.SkyMaster_Hub.config.CustomCache;
import com.platform.SkyMaster_Hub.dto.FlightScheduleDTO;
import com.platform.SkyMaster_Hub.entity.FlightSchedules;
import com.platform.SkyMaster_Hub.mapper.FlightScheduleMapper;
import com.platform.SkyMaster_Hub.repository.FlightSchedulesRepository;

@Service

public class AirLabService {

    private final AirportsRepository airportsRepository;
    private final AirlinesRepository airlinesRepository;
    private final CountriesRepository countriesRepository;
    private final CitiesRepository citiesRepository;
    private final FlightSchedulesRepository schedulesRepository;
    private final WebClient.Builder webClientBuilder;
    private final AirportMapper airportMapper;
    private final AirlineMapper airlineMapper;
    private final CountryMapper countryMapper;
    private final CityMapper cityMapper;
    private final FlightScheduleMapper scheduleMapper;
    private final CustomCache<String, List<FlightSchedules>> schedulesCache;

    @Value("${airlab.api.key}")
    private String apiKey;

    @Value("${airlab.api.base-url}")
    private String baseUrl;

    public AirLabService(AirportsRepository airportsRepository,
            AirlinesRepository airlinesRepository,
            CountriesRepository countriesRepository,
            CitiesRepository citiesRepository,
            FlightSchedulesRepository schedulesRepository,
            WebClient.Builder webClientBuilder,
            AirportMapper airportMapper,
            AirlineMapper airlineMapper,
            CountryMapper countryMapper,
            CityMapper cityMapper,
            FlightScheduleMapper scheduleMapper) {
        this.airportsRepository = airportsRepository;
        this.airlinesRepository = airlinesRepository;
        this.countriesRepository = countriesRepository;
        this.citiesRepository = citiesRepository;
        this.schedulesRepository = schedulesRepository;
        this.webClientBuilder = webClientBuilder;
        this.airportMapper = airportMapper;
        this.airlineMapper = airlineMapper;
        this.countryMapper = countryMapper;
        this.cityMapper = cityMapper;
        this.scheduleMapper = scheduleMapper;
        this.schedulesCache = new CustomCache<>(10 * 60 * 1000L, 100);
    }

    //tai them template 
    public <T, E> List<E> fetchAndSave(String path,
            Class<T> dtoClass,
            AirLabResponse.EntityMapper<T, E> mapper,
            Function<E, Optional<E>> findExistingFunc,
            BiFunction<E, E, E> mergeFunc) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

            String rawResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path(path)
                    .queryParam("api_key", apiKey)
                    .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (rawResponse == null || rawResponse.isEmpty()) {
                throw new AirLabApiException("No data received from AirLab API");
            }

            ObjectMapper mapperJson = new ObjectMapper();
            AirLabResponse<T> apiResponse = mapperJson.readValue(rawResponse,
                    mapperJson.getTypeFactory().constructParametricType(AirLabResponse.class, dtoClass));

            if (apiResponse.getError() != null) {
                throw new AirLabApiException("AirLab API error: " + apiResponse.getError().getMessage());
            }

            List<E> entities = apiResponse.getResponse().stream()
                    .map(mapper::toEntity)
                    .collect(Collectors.toList());

            List<E> saved = entities.stream()
                    .map(entity -> findExistingFunc.apply(entity).map(e -> mergeFunc.apply(e, entity)).orElse(entity))
                    .collect(Collectors.toList());

            return saved;

        } catch (Exception e) {
            throw new AirLabApiException("Failed to fetch data from AirLab API", e);
        }
    }

    ///vi du
//     List<Cities> cities = fetchAndSave(
//         "/cities",
//         CityDTO.class,
//         cityMapper::toEntity,
//         city -> citiesRepository.findByCityCode(city.getCityCode()),
//         (existing, updated) -> {
//             existing.setLat(updated.getLat());
//             existing.setLng(updated.getLng());
//             return citiesRepository.save(existing);
//         }
// );

    /////////////////////////////////////////////////////
    public List<Airports> fetchAndSaveAirportsByCityCode(String cityCode) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();
            String rawResponse = webClient.get().uri(UriBuilder -> UriBuilder
                    .path("/airports")
                    .queryParam("api_key", apiKey)
                    .queryParam("city_code", cityCode)
                    .build())
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                return clientResponse.bodyToMono(String.class)
                                        .map(errorBody -> new AirLabApiException(
                                        String.format("AirLab API error [%s]: %s",
                                                clientResponse.statusCode(), errorBody)));
                            }).bodyToMono(String.class)
                    .block();
            if (rawResponse == null || rawResponse.isEmpty()) {
                throw new AirLabApiException("No data received from AirLab API");
            }
            AirLabResponse<AirportDTO> response;
            try {
                ObjectMapper mapper = new ObjectMapper();
                response = mapper.readValue(rawResponse,
                        new TypeReference<AirLabResponse<AirportDTO>>() {
                });
            } catch (Exception parseException) {
                throw new AirLabApiException("Failed to parse API response: " + parseException.getMessage());
            }
            if (response.getError() != null) {
                String errorMsg = response.getError().getMessage() != null
                        ? response.getError().getMessage()
                        : "Unknown API error";
                throw new AirLabApiException("AirLab API error: " + errorMsg);
            }

            if (response == null || response.getResponse() == null || response.getResponse().isEmpty()) {
                throw new AirLabApiException("No data received from AirLab API");
            }
            List<Airports> airports = response.getResponse().stream()
                    .filter(dto -> dto.getIcaoCode() != null && !dto.getIcaoCode().isEmpty())
                    .collect(Collectors.toMap(
                            AirportDTO::getIcaoCode,
                            airportMapper::toEntity,
                            (existing, replacement) -> existing // Keep first if duplicate
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());
            List<Airports> savedAirports = airports.stream()
                    .map(airport -> {
                        Optional<Airports> existing = airportsRepository.findByIcaoCode(airport.getIcaoCode());
                        if (existing.isPresent()) {
                            Airports existingAirport = existing.get();
                            existingAirport.setIataCode(airport.getIataCode());
                            existingAirport.setName(airport.getName());
                            existingAirport.setLat(airport.getLat());
                            existingAirport.setLng(airport.getLng());
                            existingAirport.setCountryCode(airport.getCountryCode());
                            existingAirport.setCity(cityCode);
                            return airportsRepository.save(existingAirport);
                        } else {
                            return airportsRepository.save(airport);
                        }
                    })
                    .collect(Collectors.toList());
            return savedAirports;
        } catch (Exception e) {
            throw new AirLabApiException("Failed to fetch airports from AirLab API", e);
        }
    }

    public List<Airports> fetchAndSaveAirports() {
        try {
            WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();
            String rawResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path("/airports")
                    .queryParam("api_key", apiKey)
                    .queryParam("_fields", "icao_code,iata_code,name,lat,lng,country_code,city")
                    .build())
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                return clientResponse.bodyToMono(String.class)
                                        .map(errorBody -> new AirLabApiException(
                                        String.format("AirLab API error [%s]: %s",
                                                clientResponse.statusCode(), errorBody)));
                            }
                    )
                    .bodyToMono(String.class)
                    .block();

            if (rawResponse == null || rawResponse.isEmpty()) {
                throw new AirLabApiException("No data received from AirLab API");
            }

            // Now parse the JSON manually with better error handling
            AirLabResponse<AirportDTO> response;
            try {
                ObjectMapper mapper = new ObjectMapper();
                response = mapper.readValue(rawResponse,
                        new TypeReference<AirLabResponse<AirportDTO>>() {
                });
            } catch (Exception parseException) {
                throw new AirLabApiException("Failed to parse API response: " + parseException.getMessage());
            }

            // Check if API returned an error
            if (response.getError() != null) {
                String errorMsg = response.getError().getMessage() != null
                        ? response.getError().getMessage()
                        : "Unknown API error";
                throw new AirLabApiException("AirLab API error: " + errorMsg);
            }

            if (response == null || response.getResponse() == null || response.getResponse().isEmpty()) {
                throw new AirLabApiException("No data received from AirLab API");
            }
            // Filter duplicates by icaoCode (keep first occurrence)
            List<Airports> airports = response.getResponse().stream()
                    .filter(dto -> dto.getIcaoCode() != null && !dto.getIcaoCode().isEmpty())
                    .collect(Collectors.toMap(
                            AirportDTO::getIcaoCode,
                            airportMapper::toEntity,
                            (existing, replacement) -> existing // Keep first if duplicate
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());

            // Update existing airports or create new ones
            List<Airports> savedAirports = airports.stream()
                    .map(airport -> {
                        Optional<Airports> existing = airportsRepository.findByIcaoCode(airport.getIcaoCode());
                        if (existing.isPresent()) {
                            Airports existingAirport = existing.get();
                            existingAirport.setIataCode(airport.getIataCode());
                            existingAirport.setName(airport.getName());
                            existingAirport.setLat(airport.getLat());
                            existingAirport.setLng(airport.getLng());
                            existingAirport.setCountryCode(airport.getCountryCode());
                            existingAirport.setCity(airport.getCity());
                            return airportsRepository.save(existingAirport);
                        } else {
                            return airportsRepository.save(airport);
                        }
                    })
                    .collect(Collectors.toList());
            return savedAirports;

        } catch (Exception e) {
            throw new AirLabApiException("Failed to fetch airports from AirLab API", e);
        }
    }

    public List<Airlines> fetchAndSaveAirlines() {
        try {
            WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

            String rawResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path("/airlines")
                    .queryParam("api_key", apiKey)
                    .queryParam("_fields", "iata_code,icao_code,name,country_code")
                    .build())
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                return clientResponse.bodyToMono(String.class)
                                        .map(errorBody -> new AirLabApiException(
                                        String.format("AirLab API error [%s]: %s",
                                                clientResponse.statusCode(), errorBody)));
                            }
                    )
                    .bodyToMono(String.class)
                    .block();

            if (rawResponse == null || rawResponse.isEmpty()) {
                throw new AirLabApiException("No data received from AirLab API");
            }

            AirLabResponse<AirlineDTO> response;
            try {
                ObjectMapper mapper = new ObjectMapper();
                response = mapper.readValue(rawResponse,
                        new TypeReference<AirLabResponse<AirlineDTO>>() {
                });
            } catch (Exception parseException) {
                throw new AirLabApiException("Failed to parse API response: " + parseException.getMessage());
            }

            if (response.getError() != null) {
                String errorMsg = response.getError().getMessage() != null
                        ? response.getError().getMessage()
                        : "Unknown API error";

                throw new AirLabApiException("AirLab API error: " + errorMsg);
            }

            if (response == null || response.getResponse() == null || response.getResponse().isEmpty()) {
                throw new AirLabApiException("No data received from AirLab API");
            }

            // Filter duplicates by iataCode (keep first occurrence)
            List<Airlines> airlines = response.getResponse().stream()
                    .filter(dto -> dto.getIataCode() != null && !dto.getIataCode().isEmpty())
                    .collect(Collectors.toMap(
                            AirlineDTO::getIataCode,
                            airlineMapper::toEntity,
                            (existing, replacement) -> existing // Keep first if duplicate
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());

            // Update existing airlines or create new ones
            List<Airlines> savedAirlines = airlines.stream()
                    .map(airline -> {
                        Optional<Airlines> existing = airlinesRepository.findByIataCode(airline.getIataCode());
                        if (existing.isPresent()) {
                            Airlines existingAirline = existing.get();
                            existingAirline.setIcaoCode(airline.getIcaoCode());
                            existingAirline.setName(airline.getName());
                            existingAirline.setCountryCode(airline.getCountryCode());
                            return airlinesRepository.save(existingAirline);
                        } else {
                            return airlinesRepository.save(airline);
                        }
                    })
                    .collect(Collectors.toList());

            return savedAirlines;

        } catch (Exception e) {
            throw new AirLabApiException("Failed to fetch airlines from AirLab API", e);
        }
    }

    public List<Airports> getAllAirports() {
        return airportsRepository.findAll();
    }

    public List<Airlines> getAllAirlines() {
        return airlinesRepository.findAll();
    }

    public Airports getAirportByIcaoCode(String icaoCode) {
        return airportsRepository.findByIcaoCode(icaoCode)
                .orElseThrow(() -> new AirLabApiException("Airport not found with ICAO code: " + icaoCode));
    }

    public Airlines getAirlineByIataCode(String iataCode) {
        return airlinesRepository.findByIataCode(iataCode)
                .orElseThrow(() -> new AirLabApiException("Airline not found with IATA code: " + iataCode));
    }

    public List<Countries> fetchAndSaveCountries() {
        try {
            WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

            String rawResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path("/countries")
                    .queryParam("api_key", apiKey)
                    .build())
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                return clientResponse.bodyToMono(String.class)
                                        .map(errorBody -> new AirLabApiException(
                                        String.format("AirLab API error [%s]: %s",
                                                clientResponse.statusCode(), errorBody)));
                            }
                    )
                    .bodyToMono(String.class)
                    .block();

            if (rawResponse == null || rawResponse.isEmpty()) {
                throw new AirLabApiException("No data received from AirLab API");
            }

            AirLabResponse<CountryDTO> response;
            try {
                ObjectMapper mapper = new ObjectMapper();
                response = mapper.readValue(rawResponse,
                        new TypeReference<AirLabResponse<CountryDTO>>() {
                });
            } catch (Exception parseException) {
                throw new AirLabApiException("Failed to parse API response: " + parseException.getMessage());
            }

            if (response.getError() != null) {
                String errorMsg = response.getError().getMessage() != null
                        ? response.getError().getMessage()
                        : "Unknown API error";
                throw new AirLabApiException("AirLab API error: " + errorMsg);
            }

            if (response == null || response.getResponse() == null || response.getResponse().isEmpty()) {
                throw new AirLabApiException("No data received from AirLab API");
            }

            List<Countries> countries = response.getResponse().stream()
                    .filter(dto -> dto.getCode() != null && !dto.getCode().isEmpty())
                    .collect(Collectors.toMap(
                            CountryDTO::getCode,
                            countryMapper::toEntity,
                            (existing, replacement) -> existing
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());

            List<Countries> savedCountries = countries.stream()
                    .map(country -> {
                        Optional<Countries> existing = countriesRepository.findByCode(country.getCode());
                        if (existing.isPresent()) {
                            Countries existingCountry = existing.get();
                            existingCountry.setCode3(country.getCode3());
                            existingCountry.setName(country.getName());
                            return countriesRepository.save(existingCountry);
                        } else {
                            return countriesRepository.save(country);
                        }
                    })
                    .collect(Collectors.toList());

            return savedCountries;

        } catch (Exception e) {
            throw new AirLabApiException("Failed to fetch countries from AirLab API", e);
        }
    }

    @Cacheable
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
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                return clientResponse.bodyToMono(String.class)
                                        .map(errorBody -> new AirLabApiException(
                                        String.format("AirLab API error [%s]: %s",
                                                clientResponse.statusCode(), errorBody)));
                            }
                    )
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

    public List<Cities> fetchAndSaveCities() {
        try {
            WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

            String rawResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                    .path("/cities")
                    .queryParam("api_key", apiKey)
                    // .queryParam("city_code", "SIN")
                    .build())
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                return clientResponse.bodyToMono(String.class)
                                        .map(errorBody -> new AirLabApiException(
                                        String.format("AirLab API error [%s]: %s",
                                                clientResponse.statusCode(), errorBody)));
                            }
                    )
                    .bodyToMono(String.class)
                    .block();
            System.out.println("RAW JSON from AirLabs /cities: " + rawResponse);

            if (rawResponse == null || rawResponse.isEmpty()) {
                throw new AirLabApiException("No data received from AirLab API");
            }

            AirLabResponse<CityDTO> response;
            try {
                ObjectMapper mapper = new ObjectMapper();
                response = mapper.readValue(rawResponse, new TypeReference<AirLabResponse<CityDTO>>() {
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

            // Filter duplicates by name and country code
            List<Cities> cities = response.getResponse().stream()
                    .filter(dto -> dto.getName() != null && !dto.getName().isEmpty())
                    .collect(Collectors.toMap(
                            dto -> dto.getName() + "_" + dto.getCountryCode(),
                            cityMapper::toEntity,
                            (existing, replacement) -> existing
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());

            // Update existing cities or create new ones
            List<Cities> savedCities = cities.stream()
                    .map(city -> {
                        Optional<Cities> existing = citiesRepository.findByCityCode(
                                city.getCityCode());
                        if (existing.isPresent()) {
                            Cities existingCity = existing.get();
                            existingCity.setLat(city.getLat());
                            existingCity.setLng(city.getLng());
                            return citiesRepository.save(existingCity);
                        } else {
                            return citiesRepository.save(city);
                        }
                    })
                    .collect(Collectors.toList());

            return savedCities;

        } catch (WebClientResponseException e) {
            throw new AirLabApiException("AirLab API returned HTTP " + e.getRawStatusCode() + ": " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new AirLabApiException("Failed to fetch cities from AirLab API", e);
        }
    }

    public List<Countries> getAllCountries() {
        return countriesRepository.findAll();
    }

    public List<Cities> getAllCities() {
        return citiesRepository.findAll();
    }

    public Countries getCountryByCode(String code) {
        return countriesRepository.findByCode(code)
                .orElseThrow(() -> new AirLabApiException("Country not found with code: " + code));
    }

    // ============= NEW SEARCH METHODS =============
    /**
     * Get cities by country code
     */
    public List<Cities> getCitiesByCountryCode(String countryCode) {
        return citiesRepository.findByCountryCode(countryCode);
    }

    /**
     * Get flight schedules by departure airport
     */
    @Cacheable(value = "schedulesByDeparture", key = "#depIata")
    public List<FlightSchedules> getSchedulesByDepartureAirport(String depIata) {
        List<FlightSchedules> cachedSchedules = schedulesCache.get(depIata);
        if (cachedSchedules != null) {
            return cachedSchedules; // trả về luôn nếu có cache hợp lệ
        }
        List<FlightSchedules> schedules = schedulesRepository.findAllByDepIata(depIata);
        if (schedules.isEmpty()) {
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
                        .onStatus(
                                status -> status.is4xxClientError() || status.is5xxServerError(),
                                clientResponse -> {
                                    return clientResponse.bodyToMono(String.class)
                                            .map(errorBody -> new AirLabApiException(
                                            String.format("AirLab API error [%s]: %s",
                                                    clientResponse.statusCode(), errorBody)));
                                }
                        )
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
                schedulesCache.put(depIata, savedSchedules);
                return savedSchedules;

            } catch (WebClientResponseException e) {
                throw new AirLabApiException("AirLab API returned HTTP " + e.getRawStatusCode() + ": " + e.getResponseBodyAsString(), e);
            } catch (Exception e) {
                e.printStackTrace();
                throw new AirLabApiException("Failed to fetch schedules from AirLab API", e);
            }
        }
        schedulesCache.put(depIata, schedules);
        return schedules;
    }

    /**
     * Get flight schedules by arrival airport
     */
    @Cacheable(value = "schedulesByArrival", key = "#arrIata")
    public List<FlightSchedules> getSchedulesByArrivalAirport(String arrIata) {
        List<FlightSchedules> cachedSchedules = schedulesCache.get(arrIata);
        if (cachedSchedules != null) {
            return cachedSchedules; // trả về luôn nếu có cache hợp lệ
        }
        List<FlightSchedules> schedules = schedulesRepository.findAllByArrIata(arrIata);
        if (schedules.isEmpty()) {
            try {
                WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();
                String rawResponse = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                        .path("/schedules")
                        .queryParam("api_key", apiKey)
                        .queryParam("arr_iata", arrIata)
                        .queryParam("_fields", "flight_iata,flight_icao,airline_iata,duration,dep_iata,dep_icao,arr_iata,arr_icao,dep_time,arr_time")
                        .build())
                        .retrieve()
                        .onStatus(
                                status -> status.is4xxClientError() || status.is5xxServerError(),
                                clientResponse -> {
                                    return clientResponse.bodyToMono(String.class)
                                            .map(errorBody -> new AirLabApiException(
                                            String.format("AirLab API error [%s]: %s",
                                                    clientResponse.statusCode(), errorBody)));
                                }
                        )
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
                schedulesCache.put(arrIata, schedules);
                return savedSchedules;

            } catch (WebClientResponseException e) {
                throw new AirLabApiException("AirLab API returned HTTP " + e.getRawStatusCode() + ": " + e.getResponseBodyAsString(), e);
            } catch (Exception e) {
                e.printStackTrace();
                throw new AirLabApiException("Failed to fetch schedules from AirLab API", e);
            }
        }
        schedulesCache.put(arrIata, schedules);
        return schedules;
    }

    /**
     * Get airports by country code
     */
    public List<Airports> getAirportsByCountryCode(String countryCode) {
        return airportsRepository.findByCountryCode(countryCode);
    }

}
