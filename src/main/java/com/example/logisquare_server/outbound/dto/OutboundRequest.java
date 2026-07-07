package com.example.logisquare_server.outbound.dto;

public record OutboundRequest(
        Long inventoryId,
        Integer quantity
) {
}
