package com.example.logisquare_server.auth.dto;

public record CreateWorkerAccountRequest(
        String loginId,
        String password,
        String name,
        String employeeNo
) {
}
