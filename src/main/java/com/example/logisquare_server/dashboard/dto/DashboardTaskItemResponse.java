package com.example.logisquare_server.dashboard.dto;

public record DashboardTaskItemResponse(
        Long taskId,
        String itemName,
        Integer quantity,
        String status
) {
}
