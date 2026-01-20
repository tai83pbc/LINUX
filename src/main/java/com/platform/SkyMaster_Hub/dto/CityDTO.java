package com.platform.SkyMaster_Hub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CityDTO {
    @JsonProperty("city_code")
    private String cityCode;

    @JsonProperty("name")
    private String name;
    
    @JsonProperty("country_code")
    private String countryCode;
    
    @JsonProperty("lat")
    private Double lat;
    
    @JsonProperty("lng")
    private Double lng;
}
