package com.platform.SkyMaster_Hub.mapper;

import com.platform.SkyMaster_Hub.dto.AirlineDTO;
import com.platform.SkyMaster_Hub.entity.Airlines;
import org.springframework.stereotype.Component;

@Component
public class AirlineMapper {
    
    public Airlines toEntity(AirlineDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Airlines airline = new Airlines();
        airline.setIataCode(dto.getIataCode());
        airline.setIcaoCode(dto.getIcaoCode());
        airline.setName(dto.getName());
        airline.setCountryCode(dto.getCountryCode());
        return airline;
    }
    
    public AirlineDTO toDto(Airlines entity) {
        if (entity == null) {
            return null;
        }
        
        AirlineDTO dto = new AirlineDTO();
        dto.setIataCode(entity.getIataCode());
        dto.setIcaoCode(entity.getIcaoCode());
        dto.setName(entity.getName());
        dto.setCountryCode(entity.getCountryCode());
        return dto;
    }
}
