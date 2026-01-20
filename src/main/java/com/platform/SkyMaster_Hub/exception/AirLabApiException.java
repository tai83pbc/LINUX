package com.platform.SkyMaster_Hub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;

@Getter
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class AirLabApiException extends RuntimeException {

    private int statusCode = 500;

    public AirLabApiException(String message) {
        super(message);
    }

    public AirLabApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public AirLabApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
