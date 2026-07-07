package com.example.logisquare_server.inventory.dto;

import com.example.logisquare_server.domain.location.AreaCode;
import com.example.logisquare_server.domain.location.StorageGrade;
import java.time.LocalDateTime;

public record InventorySearchResultResponse(
        Long inventoryId,
        Long itemId,
        String sku,
        String itemName,
        String category,
        Integer quantity,
        Long locationId,
        String locationCode,
        String locationName,
        AreaCode areaCode,
        StorageGrade locationGrade,
        Boolean dangerArea,
        Integer posX,
        Integer posY,
        Integer rowIndex,
        Integer columnIndex,
        String locationLabel,
        Integer capacity,
        LocalDateTime lastMovedAt
) {
}
