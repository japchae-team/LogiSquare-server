package com.example.logisquare_server.inbound.service;

import com.example.logisquare_server.domain.location.StorageGrade;
import com.example.logisquare_server.domain.location.StorageLocation;
import com.example.logisquare_server.inbound.dto.InboundRecommendRequest;
import com.example.logisquare_server.inbound.dto.InboundRecommendResponse;
import com.example.logisquare_server.inbound.exception.InboundRecommendationException;
import com.example.logisquare_server.repository.StorageLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InboundService {

    private static final int GRADE_A_MIN_QUANTITY = 150;
    private static final int GRADE_B_MIN_QUANTITY = 50;

    private final StorageLocationRepository storageLocationRepository;

    public InboundService(StorageLocationRepository storageLocationRepository) {
        this.storageLocationRepository = storageLocationRepository;
    }

    public InboundRecommendResponse recommend(InboundRecommendRequest request) {
        validateRecommendRequest(request);

        StorageGrade recommendedGrade = resolveGrade(request.quantity());
        StorageLocation location = storageLocationRepository
                .findFirstAvailableLocationByGrade(recommendedGrade, request.quantity())
                .orElseThrow(() -> new InboundRecommendationException(
                        "No available storage location for grade " + recommendedGrade
                ));

        return new InboundRecommendResponse(
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
}
