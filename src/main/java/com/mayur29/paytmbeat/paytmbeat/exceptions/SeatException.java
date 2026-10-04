package com.mayur29.paytmbeat.paytmbeat.exceptions;

import lombok.Getter;

@Getter
public class SeatException extends RuntimeException {
    private final String code;

    public SeatException(String code,String message) {
        super(message);
        this.code = code;
    }

}
