package com.platform.SkyMaster_Hub.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.platform.SkyMaster_Hub.entity.Cities;

@Repository
public interface CitiesRepository extends JpaRepository<Cities, Long> {

    Optional<Cities> findByCityCode(String cityCode);

    List<Cities> findByCountryCode(String countryCode);
}
