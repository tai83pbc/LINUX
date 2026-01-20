package com.platform.SkyMaster_Hub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AirlineDTO {
    @JsonProperty("iata_code")
    private String iataCode;
    
    @JsonProperty("icao_code")
    private String icaoCode;
    
    private String name;
    
    @JsonProperty("country_code")
    private String countryCode;
}
