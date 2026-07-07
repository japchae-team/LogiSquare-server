package com.example.logisquare_server.auth.dto;

import java.time.LocalDateTime;

public record TaskCallResponse(
        Long taskId,
        Long assignmentId,
        Long workerId,
        String employeeNo,
        String workerName,
        String assignmentStatus,
        String nearestApCode,
        Integer rssi,
        Long locationId,
        String locationCode,
        Integer locationPosX,
        Integer locationPosY,
        LocalDateTime calledAt
) {
}
