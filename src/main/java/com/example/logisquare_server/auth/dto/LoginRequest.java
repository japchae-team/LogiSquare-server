package com.example.logisquare_server.auth.dto;

public record LoginRequest(
        String username,
        String password
) {
}
