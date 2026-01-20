package com.platform.SkyMaster_Hub.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AirLabResponse<T> {

    private List<T> response;
    private ErrorResponse error;
    private RequestInfo request;
    private String terms;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorResponse {

        private String message;
        private String code;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RequestInfo {

        private String lang;
        private String currency;
        private Integer time;
        private String id;
        private String server;
        private String host;
    }

    @FunctionalInterface
    public interface EntityMapper<T, R> {

        R toEntity(T dto);
    }
}
