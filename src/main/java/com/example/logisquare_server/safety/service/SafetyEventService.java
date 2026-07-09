package com.example.logisquare_server.safety.service;

import com.example.logisquare_server.domain.device.CctvCamera;
import com.example.logisquare_server.domain.location.StorageLocation;
import com.example.logisquare_server.domain.safety.SafetyEvent;
import com.example.logisquare_server.repository.CctvCameraRepository;
import com.example.logisquare_server.repository.SafetyEventRepository;
import com.example.logisquare_server.repository.StorageLocationRepository;
import com.example.logisquare_server.safety.dto.AiCctvSafetyEventData;
import com.example.logisquare_server.safety.dto.AiCctvSafetyEventRequest;
import com.example.logisquare_server.safety.dto.AiCctvSafetyEventResponse;
import com.example.logisquare_server.safety.exception.SafetyEventException;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SafetyEventService {

    private static final String DEFAULT_SOURCE_TYPE = "CCTV";
    private static final String DEFAULT_EVENT_TYPE = "SAFETY_GEAR_CHECK";
    private static final String DETECTED_STATUS = "DETECTED";
    private static final String CLEAR_STATUS = "CLEAR";

    private final SafetyEventRepository safetyEventRepository;
    private final CctvCameraRepository cctvCameraRepository;
    private final StorageLocationRepository storageLocationRepository;

    public SafetyEventService(
            SafetyEventRepository safetyEventRepository,
            CctvCameraRepository cctvCameraRepository,
            StorageLocationRepository storageLocationRepository
    ) {
        this.safetyEventRepository = safetyEventRepository;
        this.cctvCameraRepository = cctvCameraRepository;
        this.storageLocationRepository = storageLocationRepository;
    }

    @Transactional
    public AiCctvSafetyEventResponse createAiCctvEvent(AiCctvSafetyEventRequest request) {
        if (request == null) {
            throw new SafetyEventException("request body is required.");
        }

        AiCctvSafetyEventData payload = request.payload();
        StorageContext storageContext = resolveStorageContext(payload);
        LocalDateTime occurredAt = payload.occurredAt() != null ? payload.occurredAt() : LocalDateTime.now();
        String status = resolveStatus(payload.helmetWorn(), payload.vestWorn());

        SafetyEvent safetyEvent = safetyEventRepository.save(new SafetyEvent(
                null,
                storageContext.storageLocation(),
                storageContext.camera(),
                defaultIfBlank(payload.eventType(), DEFAULT_EVENT_TYPE),
                defaultIfBlank(payload.sourceType(), DEFAULT_SOURCE_TYPE),
                status,
                payload.annotatedImageUrl(),
                payload.confidenceScore(),
                payload.helmetWorn(),
                payload.vestWorn(),
                null,
                null,
                null,
                null,
                null,
                occurredAt,
                null
        ));

        return new AiCctvSafetyEventResponse(
                safetyEvent.getId(),
                safetyEvent.getStatus(),
                safetyEvent.getOccurredAt()
        );
    }

    private StorageContext resolveStorageContext(AiCctvSafetyEventData payload) {
        if (payload == null) {
            throw new SafetyEventException("safety event data is required.");
        }

        if (hasText(payload.cameraCode())) {
            CctvCamera camera = cctvCameraRepository.findByCameraCode(payload.cameraCode())
                    .orElseThrow(() -> new SafetyEventException("cameraCode not found: " + payload.cameraCode()));
            return new StorageContext(camera.getStorageLocation(), camera);
        }

        if (payload.storageLocationId() != null) {
            StorageLocation storageLocation = storageLocationRepository.findById(payload.storageLocationId())
                    .orElseThrow(() -> new SafetyEventException(
                            "storageLocationId not found: " + payload.storageLocationId()
                    ));
            return new StorageContext(storageLocation, null);
        }

        if (hasText(payload.storageLocationCode())) {
            StorageLocation storageLocation = storageLocationRepository.findByCode(payload.storageLocationCode())
                    .orElseThrow(() -> new SafetyEventException(
                            "storageLocationCode not found: " + payload.storageLocationCode()
                    ));
            return new StorageContext(storageLocation, null);
        }

        StorageLocation defaultLocation = storageLocationRepository.findFirstByActiveTrueOrderByIdAsc()
                .orElseThrow(() -> new SafetyEventException("No active storage location exists."));
        return new StorageContext(defaultLocation, null);
    }

    private String resolveStatus(Boolean helmetWorn, Boolean vestWorn) {
        if (Boolean.FALSE.equals(helmetWorn) || Boolean.FALSE.equals(vestWorn)) {
            return DETECTED_STATUS;
        }
        return CLEAR_STATUS;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        if (!hasText(value)) {
            return defaultValue;
        }
        return value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record StorageContext(
            StorageLocation storageLocation,
            CctvCamera camera
    ) {
    }
}
