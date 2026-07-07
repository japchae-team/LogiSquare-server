package com.example.logisquare_server.dashboard.dto;

import java.time.LocalDateTime;

public record DashboardInboundItemResponse(
        Long taskId,
        Long itemId,
        String itemName,
        Integer quantity,
        String targetLocationCode,
        LocalDateTime requestedAt
) {
}
