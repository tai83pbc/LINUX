package com.platform.SkyMaster_Hub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.platform.SkyMaster_Hub.entity.Airports;

@Repository
public interface AirportsRepository extends JpaRepository<Airports, Long> {

    Optional<Airports> findByIcaoCode(String icaoCode);

    boolean existsByIcaoCode(String icaoCode);

    boolean existsByCity(String cityCode);

    List<Airports> findByCountryCode(String countryCode);
}
