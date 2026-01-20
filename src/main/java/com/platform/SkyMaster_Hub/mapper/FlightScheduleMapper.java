package com.platform.SkyMaster_Hub.mapper;

import org.springframework.stereotype.Component;

import com.platform.SkyMaster_Hub.dto.FlightScheduleDTO;
import com.platform.SkyMaster_Hub.entity.FlightSchedules;

@Component
public class FlightScheduleMapper {

    public FlightSchedules toEntity(FlightScheduleDTO dto) {
        if (dto == null) {
            return null;
        }

        FlightSchedules schedule = new FlightSchedules();
        schedule.setFlightIata(dto.getFlightIata());
        schedule.setFlightIcao(dto.getFlightIcao());
        schedule.setAirlineIata(dto.getAirlineIata());
        schedule.setDepIata(dto.getDepIata());
        schedule.setDepIcao(dto.getDepIcao());
        schedule.setDepTime(dto.getDepTime());
        schedule.setArrIata(dto.getArrIata());
        schedule.setArrIcao(dto.getArrIcao());
        schedule.setArrTime(dto.getArrTime());
        schedule.setDuration(dto.getDuration());
        schedule.setFetchAt(java.time.LocalDateTime.now());

        return schedule;
    }

    public FlightScheduleDTO toDTO(FlightSchedules entity) {
        if (entity == null) {
            return null;
        }

        FlightScheduleDTO schedule = new FlightScheduleDTO();
        schedule.setFlightIata(entity.getFlightIata());
        schedule.setFlightIcao(entity.getFlightIcao());
        schedule.setAirlineIata(entity.getAirlineIata());
        schedule.setDepIata(entity.getDepIata());
        schedule.setDepIcao(entity.getDepIcao());
        schedule.setDepTime(entity.getDepTime());
        schedule.setArrIata(entity.getArrIata());
        schedule.setArrIcao(entity.getArrIcao());
        schedule.setArrTime(entity.getArrTime());
        schedule.setDuration(entity.getDuration());
        return schedule;
    }
}
