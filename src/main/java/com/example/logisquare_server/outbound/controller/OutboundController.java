package com.example.logisquare_server.outbound.controller;

import com.example.logisquare_server.outbound.dto.OutboundRequest;
import com.example.logisquare_server.outbound.dto.OutboundResponse;
import com.example.logisquare_server.outbound.service.OutboundService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/outbound")
public class OutboundController {

    private final OutboundService outboundService;

    public OutboundController(OutboundService outboundService) {
        this.outboundService = outboundService;
    }

    @PostMapping
    public ResponseEntity<OutboundResponse> createOutboundTask(@RequestBody OutboundRequest request) {
        return ResponseEntity.ok(outboundService.createOutboundTask(request));
    }
}
