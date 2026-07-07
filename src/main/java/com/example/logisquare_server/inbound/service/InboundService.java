package com.example.logisquare_server.inbound.service;

import com.example.logisquare_server.domain.item.Item;
import com.example.logisquare_server.domain.item.RotationGrade;
import com.example.logisquare_server.domain.location.StorageGrade;
import com.example.logisquare_server.domain.location.StorageLocation;
import com.example.logisquare_server.domain.task.WorkTask;
import com.example.logisquare_server.inbound.dto.InboundRecommendRequest;
import com.example.logisquare_server.inbound.dto.InboundRecommendResponse;
import com.example.logisquare_server.inbound.exception.InboundRecommendationException;
import com.example.logisquare_server.repository.ItemRepository;
import com.example.logisquare_server.repository.StorageLocationRepository;
import com.example.logisquare_server.repository.WorkTaskRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InboundService {

    private static final int GRADE_A_MIN_QUANTITY = 150;
    private static final int GRADE_B_MIN_QUANTITY = 50;
    private static final String INBOUND_TASK_TYPE = "INBOUND";
    private static final String PENDING_TASK_STATUS = "PENDING";
    private static final DateTimeFormatter SKU_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final ItemRepository itemRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final WorkTaskRepository workTaskRepository;

    public InboundService(
            ItemRepository itemRepository,
            StorageLocationRepository storageLocationRepository,
            WorkTaskRepository workTaskRepository
    ) {
        this.itemRepository = itemRepository;
        this.storageLocationRepository = storageLocationRepository;
        this.workTaskRepository = workTaskRepository;
    }

    @Transactional
    public InboundRecommendResponse recommend(InboundRecommendRequest request) {
        validateRecommendRequest(request);

        StorageGrade recommendedGrade = resolveGrade(request.quantity());
        StorageLocation location = storageLocationRepository
                .findFirstAvailableLocationByGrade(recommendedGrade, request.quantity())
                .orElseThrow(() -> new InboundRecommendationException(
                        "No available storage location for grade " + recommendedGrade
                ));
        Item item = itemRepository.save(new Item(
                createSku(),
                request.itemName(),
                null,
                RotationGrade.valueOf(recommendedGrade.name()),
                request.quantity()
        ));
        WorkTask task = workTaskRepository.save(new WorkTask(
                INBOUND_TASK_TYPE,
                item,
                null,
                location,
                request.quantity(),
                PENDING_TASK_STATUS,
                null,
                null,
                null
        ));

        return new InboundRecommendResponse(
                task.getId(),
                task.getStatus(),
                item.getId(),
                item.getSku(),
                request.itemName(),
                request.quantity(),
                recommendedGrade,
                location.getId(),
                location.getCode(),
                location.getName(),
                location.getAreaCode(),
                location.getDangerArea(),
                location.getPosX(),
                location.getPosY(),
                location.getCapacity()
        );
    }

    private void validateRecommendRequest(InboundRecommendRequest request) {
        if (request == null || request.itemName() == null || request.itemName().isBlank()) {
            throw new InboundRecommendationException("itemName is required.");
        }
        if (request.quantity() == null || request.quantity() <= 0) {
            throw new InboundRecommendationException("quantity must be greater than 0.");
        }
    }

    private StorageGrade resolveGrade(int quantity) {
        if (quantity >= GRADE_A_MIN_QUANTITY) {
            return StorageGrade.A;
        }
        if (quantity >= GRADE_B_MIN_QUANTITY) {
            return StorageGrade.B;
        }
        return StorageGrade.C;
    }

    private String createSku() {
        String sku = "INB-" + LocalDateTime.now().format(SKU_TIME_FORMATTER);
        if (!itemRepository.existsBySku(sku)) {
            return sku;
        }
        return sku + "-" + System.nanoTime();
    }
}
