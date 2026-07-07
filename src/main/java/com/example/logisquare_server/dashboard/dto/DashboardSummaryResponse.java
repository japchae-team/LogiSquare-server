package com.example.logisquare_server.dashboard.dto;

public record DashboardSummaryResponse(
        long inProgressTaskCount,
        long availableWorkerCount,
        long safetyViolationCount,
        long pendingInboundCount
) {
}
