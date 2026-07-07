package com.example.logisquare_server.inventory.dto;

import java.util.List;

public record InventorySearchResponse(
        String keyword,
        int resultCount,
        int totalInventoryCount,
        int totalQuantity,
        InventorySearchResultResponse selectedResult,
        List<InventorySearchResultResponse> searchResults,
        List<InventorySearchResultResponse> inventoryItems,
        List<InventorySlotResponse> slots
) {
}
