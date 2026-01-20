package com.platform.SkyMaster_Hub.entity;

import jakarta.persistence.Entity;
import lombok.Data;

import java.time.LocalDateTime;

import jakarta.persistence.*;


@Entity
@Table(name = "flight_schedules")
@Data
public class FlightSchedules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_iata")
    private String flightIata;

    @Column(name = "flight_icao")
    private String flightIcao;

    @Column(name = "airline_iata")
    private String airlineIata;

    @Column (name = "dep_iata")
    private String depIata;

    @Column(name = "dep_icao")
    private String depIcao;

    @Column(name = "dep_time")
    private String depTime;

    @Column (name = "arr_iata")
    private String arrIata;

    @Column (name = "arr_icao")
    private String arrIcao;

    @Column(name = "arr_time")
    private String arrTime;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "fetch_at")
    private LocalDateTime fetchAt;

}
