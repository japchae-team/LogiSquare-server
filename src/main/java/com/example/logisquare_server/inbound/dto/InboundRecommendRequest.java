package com.example.logisquare_server.inbound.dto;

public record InboundRecommendRequest(
        String itemName,
        Integer quantity
) {
}
