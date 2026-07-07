package com.example.logisquare_server.inbound.exception;

import com.example.logisquare_server.auth.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class InboundExceptionHandler {

    @ExceptionHandler(InboundRecommendationException.class)
    public ResponseEntity<ErrorResponse> handleInboundRecommendationException(
            InboundRecommendationException exception
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(exception.getMessage()));
    }
}
