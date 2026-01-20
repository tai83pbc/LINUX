package com.platform.SkyMaster_Hub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AirportDTO {
    @JsonProperty("icao_code")
    private String icaoCode;
    
    @JsonProperty("iata_code")
    private String iataCode;
    
    private String name;
    
    private Double lat;
    
    private Double lng;
    
    @JsonProperty("country_code")
    private String countryCode;
    
    private String city;
}
