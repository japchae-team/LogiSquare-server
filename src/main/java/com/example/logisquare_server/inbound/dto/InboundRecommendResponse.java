package com.example.logisquare_server.inbound.dto;

import com.example.logisquare_server.domain.location.AreaCode;
import com.example.logisquare_server.domain.location.StorageGrade;

public record InboundRecommendResponse(
        String itemName,
        Integer quantity,
        StorageGrade recommendedGrade,
        Long locationId,
        String locationCode,
        String locationName,
        AreaCode areaCode,
        Boolean dangerArea,
        Integer posX,
        Integer posY,
        Integer capacity
) {
}
