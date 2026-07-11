package com.example.logisquare_server.safety.dto;

public record AssignSafetyEventWorkerRequest(
        Long workerId,
        String employeeNo,
        Long assignedByUserId
) {
}
