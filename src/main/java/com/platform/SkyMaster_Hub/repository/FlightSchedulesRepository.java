package com.platform.SkyMaster_Hub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.platform.SkyMaster_Hub.entity.FlightSchedules;

public interface FlightSchedulesRepository extends JpaRepository<FlightSchedules, Long> {

    Optional<FlightSchedules> findByDepIata(String iataCode);

    Optional<FlightSchedules> findByFlightIataAndDepIataAndArrIata(String flightIata, String depIata, String arrIata);

    List<FlightSchedules> findAllByDepIata(String depIata);

    List<FlightSchedules> findAllByArrIata(String arrIata);
}
