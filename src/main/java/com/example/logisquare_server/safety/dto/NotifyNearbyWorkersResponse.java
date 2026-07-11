package com.example.logisquare_server.safety.dto;

import java.time.LocalDateTime;
import java.util.List;

public record NotifyNearbyWorkersResponse(
        Long eventId,
        Long storageLocationId,
        String storageLocationCode,
        String nearestAccessPointCode,
        int notifiedCount,
        List<NotifiedWorkerResponse> notifiedWorkers,
        LocalDateTime notifiedAt
) {
}
