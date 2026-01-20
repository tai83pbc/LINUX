package com.platform.SkyMaster_Hub.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FlightScheduleDTO {

    @JsonProperty("flight_iata")
    private String flightIata;

    @JsonProperty("flight_icao")
    private String flightIcao;

    @JsonProperty("airline_iata")
    private String airlineIata;

    @JsonProperty("dep_iata")
    private String depIata;

    @JsonProperty("dep_icao")
    private String depIcao;

    @JsonProperty("dep_time")
    private String depTime;

    @JsonProperty("arr_iata")
    private String arrIata;

    @JsonProperty("arr_icao")
    private String arrIcao;

    @JsonProperty("arr_time")
    private String arrTime;

    @JsonProperty("duration")
    private Integer duration;

    @JsonProperty("cs_airline_iata")
    private String csAirlineIata;

    @JsonProperty("cs_flight_number")
    private String csFlightNumber;

}
