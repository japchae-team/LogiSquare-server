package com.example.logisquare_server.auth.dto;

import java.time.LocalDateTime;

public record WorkerAssignmentResponse(
        Long assignmentId,
        Long taskId,
        String status,
        String taskType,
        Long itemId,
        String itemName,
        Integer quantity,
        Long sourceLocationId,
        String sourceLocationCode,
        Long targetLocationId,
        String targetLocationCode,
        LocalDateTime calledAt,
        LocalDateTime respondedAt,
        LocalDateTime completedAt
) {
}
