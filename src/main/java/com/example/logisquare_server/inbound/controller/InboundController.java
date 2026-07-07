package com.example.logisquare_server.inbound.controller;

import com.example.logisquare_server.inbound.dto.InboundRecommendRequest;
import com.example.logisquare_server.inbound.dto.InboundRecommendResponse;
import com.example.logisquare_server.inbound.service.InboundService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inbound")
public class InboundController {

    private final InboundService inboundService;

    public InboundController(InboundService inboundService) {
        this.inboundService = inboundService;
    }

    @PostMapping("/recommend")
    public ResponseEntity<InboundRecommendResponse> recommend(@RequestBody InboundRecommendRequest request) {
        return ResponseEntity.ok(inboundService.recommend(request));
    }
}
