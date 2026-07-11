package com.example.logisquare_server.safety.service;

import com.example.logisquare_server.domain.device.CctvCamera;
import com.example.logisquare_server.domain.device.WifiAccessPoint;
import com.example.logisquare_server.domain.location.StorageLocation;
import com.example.logisquare_server.domain.safety.SafetyEvent;
import com.example.logisquare_server.domain.user.User;
import com.example.logisquare_server.domain.worker.Worker;
import com.example.logisquare_server.repository.CctvCameraRepository;
import com.example.logisquare_server.repository.SafetyEventRepository;
import com.example.logisquare_server.repository.StorageLocationRepository;
import com.example.logisquare_server.repository.UserRepository;
import com.example.logisquare_server.repository.WifiAccessPointRepository;
import com.example.logisquare_server.repository.WorkerRepository;
import com.example.logisquare_server.safety.dto.AiCctvSafetyEventData;
import com.example.logisquare_server.safety.dto.AiCctvSafetyEventRequest;
import com.example.logisquare_server.safety.dto.AiCctvSafetyEventResponse;
import com.example.logisquare_server.safety.dto.AssignSafetyEventWorkerRequest;
import com.example.logisquare_server.safety.dto.NotifiedWorkerResponse;
import com.example.logisquare_server.safety.dto.NotifyNearbyWorkersRequest;
import com.example.logisquare_server.safety.dto.NotifyNearbyWorkersResponse;
import com.example.logisquare_server.safety.dto.ResolveSafetyEventRequest;
import com.example.logisquare_server.safety.dto.SafetyEventActionResponse;
import com.example.logisquare_server.safety.dto.SafetyEventDetailResponse;
import com.example.logisquare_server.safety.dto.SafetyEventListResponse;
import com.example.logisquare_server.safety.dto.SafetyEventSummaryResponse;
import com.example.logisquare_server.safety.exception.SafetyEventException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SafetyEventService {

    private static final String DEFAULT_SOURCE_TYPE = "CCTV";
    private static final String DEFAULT_EVENT_TYPE = "SAFETY_GEAR_CHECK";
    private static final String DETECTED_STATUS = "DETECTED";
    private static final String CLEAR_STATUS = "CLEAR";
    private static final String RESOLVED_STATUS = "RESOLVED";
    private static final int DEFAULT_NOTIFY_WORKER_COUNT = 5;
    private static final int MAX_NOTIFY_WORKER_COUNT = 20;
    private static final int ALARM_TTL_SECONDS = 300;

    private final SafetyEventRepository safetyEventRepository;
    private final CctvCameraRepository cctvCameraRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final WorkerRepository workerRepository;
    private final UserRepository userRepository;
    private final WifiAccessPointRepository wifiAccessPointRepository;
    private final StringRedisTemplate redisTemplate;

    public SafetyEventService(
            SafetyEventRepository safetyEventRepository,
            CctvCameraRepository cctvCameraRepository,
            StorageLocationRepository storageLocationRepository,
            WorkerRepository workerRepository,
            UserRepository userRepository,
            WifiAccessPointRepository wifiAccessPointRepository,
            StringRedisTemplate redisTemplate
    ) {
        this.safetyEventRepository = safetyEventRepository;
        this.cctvCameraRepository = cctvCameraRepository;
        this.storageLocationRepository = storageLocationRepository;
        this.workerRepository = workerRepository;
        this.userRepository = userRepository;
        this.wifiAccessPointRepository = wifiAccessPointRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional(readOnly = true)
    public SafetyEventListResponse getEvents(String eventType, String status, String storageLocationCode) {
        List<SafetyEvent> events = findEvents(eventType, status).stream()
                .filter(event -> !hasText(storageLocationCode)
                        || event.getStorageLocation().getCode().equalsIgnoreCase(storageLocationCode))
                .toList();

        return new SafetyEventListResponse(
                events.size(),
                events.stream().filter(event -> isActiveStatus(event.getStatus())).count(),
                events.stream().filter(event -> RESOLVED_STATUS.equals(event.getStatus())).count(),
                events.stream().map(this::toSummaryResponse).toList()
        );
    }

    @Transactional(readOnly = true)
    public SafetyEventDetailResponse getEvent(Long eventId) {
        return toDetailResponse(findEvent(eventId));
    }

    @Transactional
    public SafetyEventActionResponse assignWorker(Long eventId, AssignSafetyEventWorkerRequest request) {
        if (request == null) {
            throw new SafetyEventException("request body is required.");
        }

        SafetyEvent event = findEvent(eventId);
        Worker worker = findWorker(request.workerId(), request.employeeNo());
        User assignedBy = findOptionalUser(request.assignedByUserId());
        event.assignWorker(worker, assignedBy, LocalDateTime.now());

        return toActionResponse(event);
    }

    @Transactional
    public SafetyEventActionResponse resolve(Long eventId, ResolveSafetyEventRequest request) {
        SafetyEvent event = findEvent(eventId);
        User resolvedBy = request != null ? findOptionalUser(request.resolvedByUserId()) : null;
        String resolutionMemo = request != null ? request.resolutionMemo() : null;
        event.resolve(resolvedBy, resolutionMemo, LocalDateTime.now());

        return toActionResponse(event);
    }

    @Transactional
    public NotifyNearbyWorkersResponse notifyNearbyWorkers(Long eventId, NotifyNearbyWorkersRequest request) {
        SafetyEvent event = findEvent(eventId);
        StorageLocation location = event.getStorageLocation();
        WifiAccessPoint nearestAccessPoint = findNearestAccessPoint(location);
        int maxWorkers = resolveNotifyWorkerCount(request);
        LocalDateTime notifiedAt = LocalDateTime.now();

        List<NotifiedWorkerResponse> notifiedWorkers = workerRepository.findAll()
                .stream()
                .map(worker -> readWorkerSignal(worker, nearestAccessPoint.getApCode()))
                .flatMap(Optional::stream)
                .sorted(Comparator
                        .comparingInt(WorkerSignal::rssi)
                        .reversed()
                        .thenComparing(signal -> signal.worker().getId()))
                .limit(maxWorkers)
                .map(signal -> notifyWorker(signal.worker(), signal.rssi(), event, location, notifiedAt))
                .toList();

        return new NotifyNearbyWorkersResponse(
                event.getId(),
                location.getId(),
                location.getCode(),
                nearestAccessPoint.getApCode(),
                notifiedWorkers.size(),
                notifiedWorkers,
                notifiedAt
        );
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

    private List<SafetyEvent> findEvents(String eventType, String status) {
        if (hasText(eventType) && hasText(status)) {
            return safetyEventRepository.findAllByEventTypeAndStatusOrderByOccurredAtDesc(eventType, status);
        }
        if (hasText(eventType)) {
            return safetyEventRepository.findAllByEventTypeOrderByOccurredAtDesc(eventType);
        }
        if (hasText(status)) {
            return safetyEventRepository.findAllByStatusOrderByOccurredAtDesc(status);
        }
        return safetyEventRepository.findAllByOrderByOccurredAtDesc();
    }

    private SafetyEvent findEvent(Long eventId) {
        return safetyEventRepository.findById(eventId)
                .orElseThrow(() -> new SafetyEventException("Safety event not found."));
    }

    private Worker findWorker(Long workerId, String employeeNo) {
        if (workerId != null) {
            return workerRepository.findById(workerId)
                    .orElseThrow(() -> new SafetyEventException("Worker not found: " + workerId));
        }
        if (hasText(employeeNo)) {
            return workerRepository.findByEmployeeNo(employeeNo)
                    .orElseThrow(() -> new SafetyEventException("Worker not found: " + employeeNo));
        }
        throw new SafetyEventException("workerId or employeeNo is required.");
    }

    private User findOptionalUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new SafetyEventException("User not found: " + userId));
    }

    private WifiAccessPoint findNearestAccessPoint(StorageLocation location) {
        if (location.getPosX() == null || location.getPosY() == null) {
            throw new SafetyEventException("Safety event location has no coordinates.");
        }

        return wifiAccessPointRepository.findAll()
                .stream()
                .filter(accessPoint -> Boolean.TRUE.equals(accessPoint.getActive()))
                .filter(accessPoint -> accessPoint.getPosX() != null && accessPoint.getPosY() != null)
                .min(Comparator
                        .comparingInt((WifiAccessPoint accessPoint) -> squaredDistance(location, accessPoint))
                        .thenComparing(WifiAccessPoint::getApCode))
                .orElseThrow(() -> new SafetyEventException("No active Wi-Fi access points found."));
    }

    private int resolveNotifyWorkerCount(NotifyNearbyWorkersRequest request) {
        if (request == null || request.maxWorkers() == null) {
            return DEFAULT_NOTIFY_WORKER_COUNT;
        }
        if (request.maxWorkers() < 1 || request.maxWorkers() > MAX_NOTIFY_WORKER_COUNT) {
            throw new SafetyEventException("maxWorkers must be between 1 and 20.");
        }
        return request.maxWorkers();
    }

    private Optional<WorkerSignal> readWorkerSignal(Worker worker, String accessPointCode) {
        Object rawRssi = redisTemplate.opsForHash().get("worker:wifi:" + worker.getId(), "ap:" + accessPointCode);
        if (rawRssi == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(new WorkerSignal(worker, Integer.parseInt(rawRssi.toString())));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private NotifiedWorkerResponse notifyWorker(
            Worker worker,
            Integer rssi,
            SafetyEvent event,
            StorageLocation location,
            LocalDateTime notifiedAt
    ) {
        String alarmKey = "worker:alarms:" + worker.getId();
        String alarmValue = "SAFETY_EVENT"
                + "|eventId=" + event.getId()
                + "|eventType=" + event.getEventType()
                + "|locationCode=" + location.getCode()
                + "|occurredAt=" + event.getOccurredAt()
                + "|notifiedAt=" + notifiedAt;

        redisTemplate.opsForList().leftPush(alarmKey, alarmValue);
        redisTemplate.opsForList().trim(alarmKey, 0, 19);
        redisTemplate.expire(alarmKey, Duration.ofSeconds(ALARM_TTL_SECONDS));

        return new NotifiedWorkerResponse(
                worker.getId(),
                worker.getEmployeeNo(),
                worker.getUser().getName(),
                rssi,
                alarmKey
        );
    }

    private SafetyEventSummaryResponse toSummaryResponse(SafetyEvent event) {
        Worker worker = event.getWorker();
        StorageLocation location = event.getStorageLocation();
        return new SafetyEventSummaryResponse(
                event.getId(),
                event.getEventType(),
                toEventTypeLabel(event.getEventType()),
                event.getStatus(),
                toStatusLabel(event.getStatus()),
                location.getId(),
                location.getCode(),
                location.getName(),
                event.getCaptureUrl(),
                worker != null ? worker.getId() : null,
                worker != null ? worker.getEmployeeNo() : null,
                worker != null ? worker.getUser().getName() : null,
                event.getOccurredAt(),
                event.getResolvedAt()
        );
    }

    private SafetyEventDetailResponse toDetailResponse(SafetyEvent event) {
        Worker worker = event.getWorker();
        StorageLocation location = event.getStorageLocation();
        CctvCamera camera = event.getCamera();
        User assignedBy = event.getAssignedBy();
        User resolvedBy = event.getResolvedBy();

        return new SafetyEventDetailResponse(
                event.getId(),
                event.getEventType(),
                toEventTypeLabel(event.getEventType()),
                event.getSourceType(),
                event.getStatus(),
                toStatusLabel(event.getStatus()),
                event.getCaptureUrl(),
                event.getConfidenceScore(),
                event.getHelmetWorn(),
                event.getVestWorn(),
                event.getShoesWorn(),
                location.getId(),
                location.getCode(),
                location.getName(),
                location.getPosX(),
                location.getPosY(),
                camera != null ? camera.getId() : null,
                camera != null ? camera.getCameraCode() : null,
                worker != null ? worker.getId() : null,
                worker != null ? worker.getEmployeeNo() : null,
                worker != null ? worker.getUser().getName() : null,
                assignedBy != null ? assignedBy.getId() : null,
                assignedBy != null ? assignedBy.getName() : null,
                event.getAssignedAt(),
                resolvedBy != null ? resolvedBy.getId() : null,
                resolvedBy != null ? resolvedBy.getName() : null,
                event.getResolutionMemo(),
                event.getOccurredAt(),
                event.getResolvedAt()
        );
    }

    private SafetyEventActionResponse toActionResponse(SafetyEvent event) {
        Worker worker = event.getWorker();
        return new SafetyEventActionResponse(
                event.getId(),
                event.getStatus(),
                worker != null ? worker.getId() : null,
                worker != null ? worker.getEmployeeNo() : null,
                worker != null ? worker.getUser().getName() : null,
                event.getAssignedAt(),
                event.getResolvedAt()
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

    private boolean isActiveStatus(String status) {
        return !CLEAR_STATUS.equals(status) && !RESOLVED_STATUS.equals(status);
    }

    private String toEventTypeLabel(String eventType) {
        if ("DANGER_ZONE_ENTRY".equals(eventType)) {
            return "위험구역 침입";
        }
        if ("SAFETY_GEAR_CHECK".equals(eventType) || "PPE_MISSING".equals(eventType)) {
            return "보호장비 미착용";
        }
        return eventType;
    }

    private String toStatusLabel(String status) {
        if ("DETECTED".equals(status) || "OPEN".equals(status) || "VIOLATION".equals(status)) {
            return "진행 중";
        }
        if ("ASSIGNED".equals(status)) {
            return "작업자 지정";
        }
        if (RESOLVED_STATUS.equals(status) || CLEAR_STATUS.equals(status)) {
            return "조치 완료";
        }
        return status;
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

    private int squaredDistance(StorageLocation location, WifiAccessPoint accessPoint) {
        int diffX = location.getPosX() - accessPoint.getPosX();
        int diffY = location.getPosY() - accessPoint.getPosY();
        return diffX * diffX + diffY * diffY;
    }

    private record StorageContext(
            StorageLocation storageLocation,
            CctvCamera camera
    ) {
    }

    private record WorkerSignal(
            Worker worker,
            int rssi
    ) {
    }
}
