package com.platform.SkyMaster_Hub.repository;

import com.platform.SkyMaster_Hub.entity.Countries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountriesRepository extends JpaRepository<Countries, Long> {
    Optional<Countries> findByCode(String code);
}
