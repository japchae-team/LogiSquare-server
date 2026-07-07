package com.example.logisquare_server.dashboard.dto;

import java.util.List;

public record DashboardSummaryResponse(
        long inProgressTaskCount,
        long availableWorkerCount,
        long safetyViolationCount,
        long pendingInboundCount,
        List<DashboardTaskItemResponse> inProgressTasks,
        List<DashboardWorkerItemResponse> availableWorkers,
        List<DashboardSafetyItemResponse> safetyViolations,
        List<DashboardInboundItemResponse> pendingInbounds
) {
}
