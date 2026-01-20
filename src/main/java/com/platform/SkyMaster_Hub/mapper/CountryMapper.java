package com.platform.SkyMaster_Hub.mapper;

import com.platform.SkyMaster_Hub.dto.CountryDTO;
import com.platform.SkyMaster_Hub.entity.Countries;
import org.springframework.stereotype.Component;

@Component
public class CountryMapper {
    
    public Countries toEntity(CountryDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Countries country = new Countries();
        country.setCode(dto.getCode());
        country.setCode3(dto.getCode3());
        country.setName(dto.getName());
        return country;
    }
    
    public CountryDTO toDto(Countries entity) {
        if (entity == null) {
            return null;
        }
        
        CountryDTO dto = new CountryDTO();
        dto.setCode(entity.getCode());
        dto.setCode3(entity.getCode3());
        dto.setName(entity.getName());
        return dto;
    }
}
