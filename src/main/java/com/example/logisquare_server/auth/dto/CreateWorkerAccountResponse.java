package com.example.logisquare_server.auth.dto;

import com.example.logisquare_server.domain.user.UserRole;

public record CreateWorkerAccountResponse(
        Long userId,
        String loginId,
        String name,
        UserRole role,
        Long workerId,
        String employeeNo,
        String workerStatus
) {
}
