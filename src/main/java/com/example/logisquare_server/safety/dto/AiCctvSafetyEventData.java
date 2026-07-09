package com.example.logisquare_server.safety.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AiCctvSafetyEventData(
        String sourceType,
        String eventType,
        BigDecimal confidenceScore,
        Boolean helmetWorn,
        Boolean vestWorn,
        String annotatedImageUrl,
        List<AiCctvSafetyDetectionRequest> detections,
        Integer count,
        String cameraCode,
        Long storageLocationId,
        String storageLocationCode,
        LocalDateTime occurredAt
) {
}
