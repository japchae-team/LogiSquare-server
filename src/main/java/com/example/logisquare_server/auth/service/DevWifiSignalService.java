package com.example.logisquare_server.auth.service;

import com.example.logisquare_server.auth.dto.CreateDummyWifiSignalsRequest;
import com.example.logisquare_server.auth.dto.CreateDummyWifiSignalsResponse;
import com.example.logisquare_server.auth.dto.DummyWorkerWifiSignalRequest;
import com.example.logisquare_server.auth.dto.DummyWorkerWifiSignalResponse;
import com.example.logisquare_server.auth.exception.DevSeedException;
import com.example.logisquare_server.domain.device.WifiAccessPoint;
import com.example.logisquare_server.domain.worker.Worker;
import com.example.logisquare_server.repository.WifiAccessPointRepository;
import com.example.logisquare_server.repository.WorkerRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DevWifiSignalService {

    private static final int DEFAULT_TTL_SECONDS = 300;
    private static final int MIN_TTL_SECONDS = 10;
    private static final int MAX_TTL_SECONDS = 3600;
    private static final int STRONG_RSSI = -45;
    private static final int MEDIUM_RSSI = -63;
    private static final int WEAK_RSSI = -78;

    private final WorkerRepository workerRepository;
    private final WifiAccessPointRepository wifiAccessPointRepository;
    private final StringRedisTemplate redisTemplate;

    public DevWifiSignalService(
            WorkerRepository workerRepository,
            WifiAccessPointRepository wifiAccessPointRepository,
            StringRedisTemplate redisTemplate
    ) {
        this.workerRepository = workerRepository;
        this.wifiAccessPointRepository = wifiAccessPointRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional(readOnly = true)
    public CreateDummyWifiSignalsResponse createDummySignals(CreateDummyWifiSignalsRequest request) {
        int ttlSeconds = resolveTtlSeconds(request);
        List<Worker> workers = workerRepository.findAll();
        List<WifiAccessPoint> accessPoints = wifiAccessPointRepository.findAll()
                .stream()
                .filter(accessPoint -> Boolean.TRUE.equals(accessPoint.getActive()))
                .sorted((left, right) -> left.getApCode().compareTo(right.getApCode()))
                .toList();

        if (workers.isEmpty()) {
            throw new DevSeedException("No workers found.");
        }
        if (accessPoints.isEmpty()) {
            throw new DevSeedException("No active Wi-Fi access points found.");
        }

        List<DummyWorkerWifiSignalResponse> signals = hasManualSignals(request)
                ? createManualSignals(request.signals(), accessPoints, ttlSeconds)
                : workers.stream()
                        .map(worker -> createAutoWorkerSignal(worker, accessPoints, ttlSeconds))
                        .toList();

        return new CreateDummyWifiSignalsResponse(
                signals.size(),
                accessPoints.size(),
                ttlSeconds,
                signals
        );
    }

    private List<DummyWorkerWifiSignalResponse> createManualSignals(
            List<DummyWorkerWifiSignalRequest> signalRequests,
            List<WifiAccessPoint> accessPoints,
            int ttlSeconds
    ) {
        Set<String> accessPointCodes = accessPoints.stream()
                .map(WifiAccessPoint::getApCode)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);

        return signalRequests.stream()
                .map(signalRequest -> createManualWorkerSignal(signalRequest, accessPointCodes, ttlSeconds))
                .toList();
    }

    private DummyWorkerWifiSignalResponse createManualWorkerSignal(
            DummyWorkerWifiSignalRequest signalRequest,
            Set<String> accessPointCodes,
            int ttlSeconds
    ) {
        Worker worker = findWorker(signalRequest);
        Map<String, Integer> rssiByApCode = validateAndSortRssi(signalRequest, accessPointCodes);
        String strongestApCode = rssiByApCode.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new DevSeedException("At least one RSSI value is required."));
        Map<String, String> redisValues = new LinkedHashMap<>();

        rssiByApCode.forEach((apCode, rssi) -> redisValues.put("ap:" + apCode, String.valueOf(rssi)));
        String redisKey = "worker:wifi:" + worker.getId();
        redisValues.put("updatedAt", LocalDateTime.now().toString());
        redisValues.put("source", "MANUAL_DUMMY");

        redisTemplate.opsForHash().putAll(redisKey, redisValues);
        redisTemplate.expire(redisKey, Duration.ofSeconds(ttlSeconds));

        return new DummyWorkerWifiSignalResponse(
                worker.getId(),
                worker.getEmployeeNo(),
                redisKey,
                strongestApCode,
                rssiByApCode
        );
    }

    private DummyWorkerWifiSignalResponse createAutoWorkerSignal(
            Worker worker,
            List<WifiAccessPoint> accessPoints,
            int ttlSeconds
    ) {
        int strongestIndex = Math.floorMod(worker.getId().intValue() - 1, accessPoints.size());
        WifiAccessPoint strongestAccessPoint = accessPoints.get(strongestIndex);
        Map<String, String> redisValues = new LinkedHashMap<>();
        Map<String, Integer> rssiByApCode = new LinkedHashMap<>();

        for (int index = 0; index < accessPoints.size(); index++) {
            WifiAccessPoint accessPoint = accessPoints.get(index);
            int rssi = resolveRssi(index, strongestIndex);
            redisValues.put("ap:" + accessPoint.getApCode(), String.valueOf(rssi));
            rssiByApCode.put(accessPoint.getApCode(), rssi);
        }

        String redisKey = "worker:wifi:" + worker.getId();
        redisValues.put("updatedAt", LocalDateTime.now().toString());
        redisValues.put("source", "DUMMY");

        redisTemplate.opsForHash().putAll(redisKey, redisValues);
        redisTemplate.expire(redisKey, Duration.ofSeconds(ttlSeconds));

        return new DummyWorkerWifiSignalResponse(
                worker.getId(),
                worker.getEmployeeNo(),
                redisKey,
                strongestAccessPoint.getApCode(),
                rssiByApCode
        );
    }

    private Worker findWorker(DummyWorkerWifiSignalRequest signalRequest) {
        if (signalRequest == null) {
            throw new DevSeedException("signal request is required.");
        }
        if (signalRequest.workerId() != null) {
            return workerRepository.findById(signalRequest.workerId())
                    .orElseThrow(() -> new DevSeedException("Worker not found: " + signalRequest.workerId()));
        }
        if (signalRequest.employeeNo() != null && !signalRequest.employeeNo().isBlank()) {
            return workerRepository.findByEmployeeNo(signalRequest.employeeNo())
                    .orElseThrow(() -> new DevSeedException("Worker not found: " + signalRequest.employeeNo()));
        }
        throw new DevSeedException("workerId or employeeNo is required.");
    }

    private Map<String, Integer> validateAndSortRssi(
            DummyWorkerWifiSignalRequest signalRequest,
            Set<String> accessPointCodes
    ) {
        if (signalRequest.rssiByApCode() == null || signalRequest.rssiByApCode().isEmpty()) {
            throw new DevSeedException("rssiByApCode is required.");
        }

        Map<String, Integer> rssiByApCode = new LinkedHashMap<>();
        signalRequest.rssiByApCode().entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String apCode = entry.getKey();
                    Integer rssi = entry.getValue();
                    if (!accessPointCodes.contains(apCode)) {
                        throw new DevSeedException("Unknown AP code: " + apCode);
                    }
                    if (rssi == null || rssi < -100 || rssi > 0) {
                        throw new DevSeedException("RSSI must be between -100 and 0.");
                    }
                    rssiByApCode.put(apCode, rssi);
                });

        return rssiByApCode;
    }

    private int resolveRssi(int accessPointIndex, int strongestIndex) {
        if (accessPointIndex == strongestIndex) {
            return STRONG_RSSI;
        }
        if (Math.abs(accessPointIndex - strongestIndex) == 1) {
            return MEDIUM_RSSI;
        }
        return WEAK_RSSI;
    }

    private int resolveTtlSeconds(CreateDummyWifiSignalsRequest request) {
        if (request == null || request.ttlSeconds() == null) {
            return DEFAULT_TTL_SECONDS;
        }
        if (request.ttlSeconds() < MIN_TTL_SECONDS || request.ttlSeconds() > MAX_TTL_SECONDS) {
            throw new DevSeedException("ttlSeconds must be between 10 and 3600.");
        }
        return request.ttlSeconds();
    }

    private boolean hasManualSignals(CreateDummyWifiSignalsRequest request) {
        return request != null && request.signals() != null && !request.signals().isEmpty();
    }
}
