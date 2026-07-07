package com.example.logisquare_server.auth.dto;

import java.time.LocalDateTime;

public record TaskActionResponse(
        Long taskId,
        Long assignmentId,
        Long workerId,
        String taskStatus,
        String assignmentStatus,
        LocalDateTime respondedAt,
        LocalDateTime completedAt
) {
}
