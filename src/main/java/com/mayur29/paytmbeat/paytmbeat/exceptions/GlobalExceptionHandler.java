package com.mayur29.paytmbeat.paytmbeat.exceptions;

import com.mayur29.paytmbeat.paytmbeat.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", "APPEX001");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SeatException.class)
    public ResponseEntity<ErrorResponse> handleSeatException(SeatException ex) {

        ErrorResponse response = new ErrorResponse(
                ex.getCode(),
                ex.getMessage()
        );

        return ResponseEntity
                .status(ex.getStatus())
                .body(response);
    }
}
