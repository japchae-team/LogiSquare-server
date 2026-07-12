package com.example.logisquare_server.safety.controller;

import com.example.logisquare_server.safety.dto.AiCctvSafetyEventRequest;
import com.example.logisquare_server.safety.dto.AiCctvSafetyEventResponse;
import com.example.logisquare_server.safety.dto.AssignSafetyEventWorkerRequest;
import com.example.logisquare_server.safety.dto.NotifyNearbyWorkersRequest;
import com.example.logisquare_server.safety.dto.NotifyNearbyWorkersResponse;
import com.example.logisquare_server.safety.dto.ResolveSafetyEventRequest;
import com.example.logisquare_server.safety.dto.SafetyEventActionResponse;
import com.example.logisquare_server.safety.dto.SafetyEventCaptureImageResponse;
import com.example.logisquare_server.safety.dto.SafetyEventDetailResponse;
import com.example.logisquare_server.safety.dto.SafetyEventListResponse;
import com.example.logisquare_server.safety.service.SafetyEventService;
import java.time.Duration;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/safety/events")
public class SafetyEventController {

    private final SafetyEventService safetyEventService;

    public SafetyEventController(SafetyEventService safetyEventService) {
        this.safetyEventService = safetyEventService;
    }

    @GetMapping
    public ResponseEntity<SafetyEventListResponse> getEvents(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String storageLocationCode
    ) {
        return ResponseEntity.ok(safetyEventService.getEvents(eventType, status, storageLocationCode));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<SafetyEventDetailResponse> getEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(safetyEventService.getEvent(eventId));
    }

    @GetMapping("/{eventId}/capture-image")
    public ResponseEntity<byte[]> getCaptureImage(@PathVariable Long eventId) {
        SafetyEventCaptureImageResponse response = safetyEventService.getCaptureImage(eventId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.contentType()))
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                .body(response.imageBytes());
    }

    @PatchMapping("/{eventId}/assign-worker")
    public ResponseEntity<SafetyEventActionResponse> assignWorker(
            @PathVariable Long eventId,
            @RequestBody AssignSafetyEventWorkerRequest request
    ) {
        return ResponseEntity.ok(safetyEventService.assignWorker(eventId, request));
    }

    @PatchMapping("/{eventId}/resolve")
    public ResponseEntity<SafetyEventActionResponse> resolve(
            @PathVariable Long eventId,
            @RequestBody(required = false) ResolveSafetyEventRequest request
    ) {
        return ResponseEntity.ok(safetyEventService.resolve(eventId, request));
    }

    @PostMapping("/{eventId}/notify-nearby-workers")
    public ResponseEntity<NotifyNearbyWorkersResponse> notifyNearbyWorkers(
            @PathVariable Long eventId,
            @RequestBody(required = false) NotifyNearbyWorkersRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(safetyEventService.notifyNearbyWorkers(eventId, request));
    }

    @PostMapping("/ai-cctv")
    public ResponseEntity<AiCctvSafetyEventResponse> createAiCctvEvent(
            @RequestBody AiCctvSafetyEventRequest request
    ) {
        return ResponseEntity.ok(safetyEventService.createAiCctvEvent(request));
    }
}
