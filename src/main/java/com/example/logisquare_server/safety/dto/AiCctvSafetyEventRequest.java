package com.example.logisquare_server.safety.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AiCctvSafetyEventRequest(
        Boolean success,
        AiCctvSafetyEventData data,
        String message,
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
    public AiCctvSafetyEventData payload() {
        if (data != null) {
            return data;
        }
        return new AiCctvSafetyEventData(
                sourceType,
                eventType,
                confidenceScore,
                helmetWorn,
                vestWorn,
                annotatedImageUrl,
                detections,
                count,
                cameraCode,
                storageLocationId,
                storageLocationCode,
                occurredAt
        );
    }
}
