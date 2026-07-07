package com.example.logisquare_server.auth.dto;

import java.util.List;

public record CreateDummyWifiSignalsRequest(
        Integer ttlSeconds,
        List<DummyWorkerWifiSignalRequest> signals
) {
}
