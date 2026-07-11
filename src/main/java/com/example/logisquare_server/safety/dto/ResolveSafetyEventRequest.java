package com.example.logisquare_server.safety.dto;

public record ResolveSafetyEventRequest(
        Long resolvedByUserId,
        String resolutionMemo
) {
}
