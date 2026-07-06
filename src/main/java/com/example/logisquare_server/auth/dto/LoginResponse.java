package com.example.logisquare_server.auth.dto;

public record LoginResponse(
        String accessToken,
        AuthUserResponse user
) {
}
