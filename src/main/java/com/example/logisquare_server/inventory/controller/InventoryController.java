package com.example.logisquare_server.inventory.controller;

import com.example.logisquare_server.inventory.dto.InventorySearchResponse;
import com.example.logisquare_server.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/layout")
    public ResponseEntity<InventorySearchResponse> getLayout(
            @RequestParam(required = false) String itemName
    ) {
        return ResponseEntity.ok(inventoryService.getLayout(itemName));
    }

    @GetMapping("/search")
    public ResponseEntity<InventorySearchResponse> search(@RequestParam String itemName) {
        return ResponseEntity.ok(inventoryService.searchByItemName(itemName));
    }
}
