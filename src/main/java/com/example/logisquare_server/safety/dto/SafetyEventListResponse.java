package com.example.logisquare_server.safety.dto;

import java.util.List;

public record SafetyEventListResponse(
        long totalCount,
        long activeCount,
        long resolvedCount,
        List<SafetyEventSummaryResponse> events
) {
}
