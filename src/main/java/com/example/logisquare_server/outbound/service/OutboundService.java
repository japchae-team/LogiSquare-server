package com.example.logisquare_server.outbound.service;

import com.example.logisquare_server.domain.inventory.Inventory;
import com.example.logisquare_server.domain.location.StorageLocation;
import com.example.logisquare_server.domain.task.WorkTask;
import com.example.logisquare_server.outbound.dto.OutboundRequest;
import com.example.logisquare_server.outbound.dto.OutboundResponse;
import com.example.logisquare_server.outbound.exception.OutboundException;
import com.example.logisquare_server.repository.InventoryRepository;
import com.example.logisquare_server.repository.WorkTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboundService {

    private static final String OUTBOUND_TASK_TYPE = "OUTBOUND";
    private static final String PENDING_TASK_STATUS = "PENDING";

    private final InventoryRepository inventoryRepository;
    private final WorkTaskRepository workTaskRepository;

    public OutboundService(
            InventoryRepository inventoryRepository,
            WorkTaskRepository workTaskRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.workTaskRepository = workTaskRepository;
    }

    @Transactional
    public OutboundResponse createOutboundTask(OutboundRequest request) {
        validateRequest(request);

        Inventory inventory = inventoryRepository.findById(request.inventoryId())
                .orElseThrow(() -> new OutboundException("Inventory not found."));
        validateInventory(inventory, request.quantity());

        WorkTask task = workTaskRepository.save(new WorkTask(
                OUTBOUND_TASK_TYPE,
                inventory.getItem(),
                inventory.getStorageLocation(),
                null,
                request.quantity(),
                PENDING_TASK_STATUS,
                null,
                null,
                null
        ));

        return toResponse(task, inventory);
    }

    private void validateRequest(OutboundRequest request) {
        if (request == null || request.inventoryId() == null) {
            throw new OutboundException("inventoryId is required.");
        }
        if (request.quantity() == null || request.quantity() <= 0) {
            throw new OutboundException("quantity must be greater than 0.");
        }
    }

    private void validateInventory(Inventory inventory, int quantity) {
        if (inventory.getQuantity() <= 0) {
            throw new OutboundException("Inventory is empty.");
        }
        if (inventory.getQuantity() < quantity) {
            throw new OutboundException("Not enough inventory.");
        }
    }

    private OutboundResponse toResponse(WorkTask task, Inventory inventory) {
        StorageLocation location = inventory.getStorageLocation();
        return new OutboundResponse(
                task.getId(),
                task.getStatus(),
                inventory.getId(),
                inventory.getItem().getId(),
                inventory.getItem().getName(),
                task.getQuantity(),
                location.getId(),
                location.getCode(),
                location.getName(),
                location.getAreaCode(),
                location.getPosX(),
                location.getPosY(),
                inventory.getQuantity()
        );
    }
}
