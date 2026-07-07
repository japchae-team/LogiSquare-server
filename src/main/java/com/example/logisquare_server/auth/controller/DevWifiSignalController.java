package com.example.logisquare_server.auth.controller;

import com.example.logisquare_server.auth.dto.CreateDummyWifiSignalsRequest;
import com.example.logisquare_server.auth.dto.CreateDummyWifiSignalsResponse;
import com.example.logisquare_server.auth.service.DevWifiSignalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev/wifi-signals")
public class DevWifiSignalController {

    private final DevWifiSignalService devWifiSignalService;

    public DevWifiSignalController(DevWifiSignalService devWifiSignalService) {
        this.devWifiSignalService = devWifiSignalService;
    }

    @PostMapping("/dummy")
    public ResponseEntity<CreateDummyWifiSignalsResponse> createDummySignals(
            @RequestBody(required = false) CreateDummyWifiSignalsRequest request
    ) {
        return ResponseEntity.ok(devWifiSignalService.createDummySignals(request));
    }
}
