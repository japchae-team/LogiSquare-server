package com.example.logisquare_server.outbound.dto;

import com.example.logisquare_server.domain.location.AreaCode;

public record OutboundResponse(
        Long taskId,
        String taskStatus,
        Long inventoryId,
        Long itemId,
        String itemName,
        Integer quantity,
        Long sourceLocationId,
        String sourceLocationCode,
        String sourceLocationName,
        AreaCode areaCode,
        Integer sourceLocationPosX,
        Integer sourceLocationPosY,
        Integer availableQuantity
) {
}
