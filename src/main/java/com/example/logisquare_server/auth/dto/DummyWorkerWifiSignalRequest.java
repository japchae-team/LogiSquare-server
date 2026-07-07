package com.example.logisquare_server.auth.dto;

import java.util.Map;

public record DummyWorkerWifiSignalRequest(
        Long workerId,
        String employeeNo,
        Map<String, Integer> rssiByApCode
) {
}
