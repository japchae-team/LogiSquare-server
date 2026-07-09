package com.example.logisquare_server.safety.controller;

import com.example.logisquare_server.safety.dto.AiCctvSafetyEventRequest;
import com.example.logisquare_server.safety.dto.AiCctvSafetyEventResponse;
import com.example.logisquare_server.safety.service.SafetyEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/safety/events")
public class SafetyEventController {

    private final SafetyEventService safetyEventService;

    public SafetyEventController(SafetyEventService safetyEventService) {
        this.safetyEventService = safetyEventService;
    }

    @PostMapping("/ai-cctv")
    public ResponseEntity<AiCctvSafetyEventResponse> createAiCctvEvent(
            @RequestBody AiCctvSafetyEventRequest request
    ) {
        return ResponseEntity.ok(safetyEventService.createAiCctvEvent(request));
    }
}
