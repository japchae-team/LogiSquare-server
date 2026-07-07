package com.example.logisquare_server.inventory.dto;

import com.example.logisquare_server.domain.location.AreaCode;
import com.example.logisquare_server.domain.location.StorageGrade;
import java.util.List;

public record InventorySlotResponse(
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
        Integer capacity,
        Integer storedQuantity,
        Boolean occupied,
        Boolean matched,
        List<String> itemNames
) {
}
