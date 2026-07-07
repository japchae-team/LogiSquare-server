package com.example.logisquare_server.inventory.service;

import com.example.logisquare_server.domain.inventory.Inventory;
import com.example.logisquare_server.domain.item.Item;
import com.example.logisquare_server.domain.location.StorageLocation;
import com.example.logisquare_server.inventory.dto.InventorySearchResponse;
import com.example.logisquare_server.inventory.dto.InventorySearchResultResponse;
import com.example.logisquare_server.inventory.dto.InventorySlotResponse;
import com.example.logisquare_server.inventory.exception.InventorySearchException;
import com.example.logisquare_server.repository.InventoryRepository;
import com.example.logisquare_server.repository.StorageLocationRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private static final int GRID_UNIT = 5;
    private static final int DEFAULT_MAX_Y = 15;

    private final InventoryRepository inventoryRepository;
    private final StorageLocationRepository storageLocationRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            StorageLocationRepository storageLocationRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.storageLocationRepository = storageLocationRepository;
    }

    public InventorySearchResponse getLayout(String itemName) {
        String keyword = normalizeKeyword(itemName);
        List<InventorySearchResultResponse> inventoryItems = getInventoryItems();
        List<InventorySearchResultResponse> searchResults = filterByKeyword(inventoryItems, keyword);
        Set<Long> matchedLocationIds = searchResults.stream()
                .map(InventorySearchResultResponse::locationId)
                .collect(Collectors.toSet());
        List<InventorySlotResponse> slots = getSlots(matchedLocationIds);

        return new InventorySearchResponse(
                keyword,
                searchResults.size(),
                inventoryItems.size(),
                searchResults.stream().mapToInt(InventorySearchResultResponse::quantity).sum(),
                searchResults.isEmpty() ? null : searchResults.get(0),
                searchResults,
                inventoryItems,
                slots
        );
    }

    public InventorySearchResponse searchByItemName(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            throw new InventorySearchException("itemName is required.");
        }
        return getLayout(itemName);
    }

    private List<InventorySearchResultResponse> getInventoryItems() {
        return inventoryRepository.findAllByQuantityGreaterThan(0)
                .stream()
                .sorted(Comparator
                        .comparing((Inventory inventory) -> rowIndex(inventory.getStorageLocation()))
                        .thenComparing(inventory -> columnIndex(inventory.getStorageLocation()))
                        .thenComparing(inventory -> inventory.getItem().getName()))
                .map(this::toResultResponse)
                .toList();
    }

    private List<InventorySearchResultResponse> filterByKeyword(
            List<InventorySearchResultResponse> inventoryItems,
            String keyword
    ) {
        if (keyword == null) {
            return List.of();
        }

        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        return inventoryItems.stream()
                .filter(item -> item.itemName().toLowerCase(Locale.ROOT).contains(lowerKeyword))
                .toList();
    }

    private List<InventorySlotResponse> getSlots(Set<Long> matchedLocationIds) {
        Map<Long, List<Inventory>> inventoriesByLocation = inventoryRepository.findAllByQuantityGreaterThan(0)
                .stream()
                .collect(Collectors.groupingBy(inventory -> inventory.getStorageLocation().getId()));

        return storageLocationRepository.findAllByActiveTrueOrderByPosYDescPosXAscCodeAsc()
                .stream()
                .map(location -> toSlotResponse(
                        location,
                        inventoriesByLocation.getOrDefault(location.getId(), List.of()),
                        matchedLocationIds.contains(location.getId())
                ))
                .toList();
    }

    private InventorySearchResultResponse toResultResponse(Inventory inventory) {
        Item item = inventory.getItem();
        StorageLocation location = inventory.getStorageLocation();

        return new InventorySearchResultResponse(
                inventory.getId(),
                item.getId(),
                item.getSku(),
                item.getName(),
                item.getCategory(),
                inventory.getQuantity(),
                location.getId(),
                location.getCode(),
                location.getName(),
                location.getAreaCode(),
                location.getLocationGrade(),
                location.getDangerArea(),
                location.getPosX(),
                location.getPosY(),
                rowIndex(location),
                columnIndex(location),
                locationLabel(location),
                location.getCapacity(),
                inventory.getLastMovedAt()
        );
    }

    private InventorySlotResponse toSlotResponse(
            StorageLocation location,
            List<Inventory> inventories,
            boolean matched
    ) {
        int storedQuantity = inventories.stream()
                .mapToInt(Inventory::getQuantity)
                .sum();
        List<String> itemNames = inventories.stream()
                .map(inventory -> inventory.getItem().getName())
                .distinct()
                .sorted()
                .toList();

        return new InventorySlotResponse(
                location.getId(),
                location.getCode(),
                location.getName(),
                location.getAreaCode(),
                location.getLocationGrade(),
                location.getDangerArea(),
                location.getPosX(),
                location.getPosY(),
                rowIndex(location),
                columnIndex(location),
                location.getCapacity(),
                storedQuantity,
                storedQuantity > 0,
                matched,
                itemNames
        );
    }

    private String normalizeKeyword(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return null;
        }
        return itemName.trim();
    }

    private Integer rowIndex(StorageLocation location) {
        if (location.getPosY() == null) {
            return null;
        }
        return (DEFAULT_MAX_Y - location.getPosY()) / GRID_UNIT;
    }

    private Integer columnIndex(StorageLocation location) {
        if (location.getPosX() == null) {
            return null;
        }
        return location.getPosX() / GRID_UNIT;
    }

    private String locationLabel(StorageLocation location) {
        Integer rowIndex = rowIndex(location);
        Integer columnIndex = columnIndex(location);
        if (rowIndex == null || columnIndex == null) {
            return location.getCode();
        }
        return location.getAreaCode()
                + "구역 "
                + (rowIndex + 1)
                + "행 "
                + (columnIndex + 1)
                + "열 (r"
                + rowIndex
                + "-c"
                + columnIndex
                + ")";
    }
}
