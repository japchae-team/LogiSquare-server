package com.example.logisquare_server.auth.dto;

import java.util.List;

public record CreateDummyWifiSignalsResponse(
        int workerCount,
        int accessPointCount,
        int ttlSeconds,
        List<DummyWorkerWifiSignalResponse> signals
) {
}
