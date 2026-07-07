package com.example.logisquare_server.dashboard.dto;

public record DashboardWorkerItemResponse(
        Long workerId,
        String employeeNo,
        String name,
        String status
) {
}
