package com.platform.SkyMaster_Hub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CountryDTO {
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("code3")
    private String code3;
    
    @JsonProperty("name")
    private String name;
}
