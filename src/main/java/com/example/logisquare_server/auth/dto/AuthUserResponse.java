package com.example.logisquare_server.auth.dto;

import com.example.logisquare_server.domain.user.UserRole;

public record AuthUserResponse(
        Long id,
        String username,
        String name,
        UserRole role
) {
}
