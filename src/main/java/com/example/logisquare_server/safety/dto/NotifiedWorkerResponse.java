package com.example.logisquare_server.safety.dto;

public record NotifiedWorkerResponse(
        Long workerId,
        String employeeNo,
        String workerName,
        Integer rssi,
        String alarmKey
) {
}
