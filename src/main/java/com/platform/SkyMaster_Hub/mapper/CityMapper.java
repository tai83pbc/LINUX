package com.platform.SkyMaster_Hub.mapper;

import com.platform.SkyMaster_Hub.dto.CityDTO;
import com.platform.SkyMaster_Hub.entity.Cities;
import org.springframework.stereotype.Component;

@Component
public class CityMapper {
    
    public Cities toEntity(CityDTO dto) {
        Cities city = new Cities();
        city.setCityCode(dto.getCityCode());
        city.setName(dto.getName());
        city.setCountryCode(dto.getCountryCode());
        city.setLat(dto.getLat());
        city.setLng(dto.getLng());
        return city;
    }
    
     public CityDTO toDto(Cities entity) {
        CityDTO dto = new CityDTO();
        dto.setCityCode(entity.getCityCode());
        dto.setName(entity.getName());
        dto.setCountryCode(entity.getCountryCode());
        dto.setLat(entity.getLat());
        dto.setLng(entity.getLng());
        return dto;
    }
}
