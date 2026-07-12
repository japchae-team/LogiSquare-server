package com.example.logisquare_server.safety.dto;

public record SafetyEventCaptureImageResponse(
        byte[] imageBytes,
        String contentType
) {
}
