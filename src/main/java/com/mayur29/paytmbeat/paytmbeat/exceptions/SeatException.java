package com.mayur29.paytmbeat.paytmbeat.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class SeatException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public SeatException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}