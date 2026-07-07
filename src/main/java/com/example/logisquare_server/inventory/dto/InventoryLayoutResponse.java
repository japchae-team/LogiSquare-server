package com.example.logisquare_server.inventory.dto;

import java.util.List;

public record InventoryLayoutResponse(
        int totalInventoryCount,
        int totalQuantity,
        List<InventorySearchResultResponse> inventoryItems,
        List<InventorySlotResponse> slots
) {
}
