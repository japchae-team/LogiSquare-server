package com.example.logisquare_server.auth.dto;

import java.util.Map;

public record DummyWorkerWifiSignalResponse(
        Long workerId,
        String employeeNo,
        String redisKey,
        String strongestApCode,
        Map<String, Integer> rssiByApCode
) {
}
