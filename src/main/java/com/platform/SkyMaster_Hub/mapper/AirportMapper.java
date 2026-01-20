package com.platform.SkyMaster_Hub.mapper;

import com.platform.SkyMaster_Hub.dto.AirportDTO;
import com.platform.SkyMaster_Hub.entity.Airports;
import org.springframework.stereotype.Component;

@Component
public class AirportMapper {
    
    public Airports toEntity(AirportDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Airports airport = new Airports();
        airport.setIcaoCode(dto.getIcaoCode());
        airport.setIataCode(dto.getIataCode());
        airport.setName(dto.getName());
        airport.setLat(dto.getLat());
        airport.setLng(dto.getLng());
        airport.setCountryCode(dto.getCountryCode());
        airport.setCity(dto.getCity());
        return airport;
    }
    
    public AirportDTO toDto(Airports entity) {
        if (entity == null) {
            return null;
        }
        
        AirportDTO dto = new AirportDTO();
        dto.setIcaoCode(entity.getIcaoCode());
        dto.setIataCode(entity.getIataCode());
        dto.setName(entity.getName());
        dto.setLat(entity.getLat());
        dto.setLng(entity.getLng());
        dto.setCountryCode(entity.getCountryCode());
        dto.setCity(entity.getCity());
        return dto;
    }
}
