package com.platform.SkyMaster_Hub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.platform.SkyMaster_Hub.entity.Airlines;

import java.util.Optional;

@Repository
public interface AirlinesRepository extends JpaRepository<Airlines, Long> {
    Optional<Airlines> findByIataCode(String iataCode);
    boolean existsByIataCode(String iataCode);
}
