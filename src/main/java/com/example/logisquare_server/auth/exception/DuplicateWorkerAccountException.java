package com.example.logisquare_server.auth.exception;

public class DuplicateWorkerAccountException extends RuntimeException {

    public DuplicateWorkerAccountException(String message) {
        super(message);
    }
}
