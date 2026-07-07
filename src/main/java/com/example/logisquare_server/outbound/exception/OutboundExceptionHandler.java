package com.example.logisquare_server.outbound.exception;

import com.example.logisquare_server.auth.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OutboundExceptionHandler {

    @ExceptionHandler(OutboundException.class)
    public ResponseEntity<ErrorResponse> handleOutboundException(OutboundException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(exception.getMessage()));
    }
}
