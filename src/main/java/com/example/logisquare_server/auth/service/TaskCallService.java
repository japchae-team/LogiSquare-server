package com.example.logisquare_server.auth.service;

import com.example.logisquare_server.auth.dto.TaskCallResponse;
import com.example.logisquare_server.auth.exception.TaskCallException;
import com.example.logisquare_server.domain.device.WifiAccessPoint;
import com.example.logisquare_server.domain.location.StorageLocation;
import com.example.logisquare_server.domain.task.TaskAssignment;
import com.example.logisquare_server.domain.task.WorkTask;
import com.example.logisquare_server.domain.worker.Worker;
import com.example.logisquare_server.repository.TaskAssignmentRepository;
import com.example.logisquare_server.repository.WifiAccessPointRepository;
import com.example.logisquare_server.repository.WorkTaskRepository;
import com.example.logisquare_server.repository.WorkerRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskCallService {

    private static final String CALLED_STATUS = "CALLED";
    private static final int ALARM_TTL_SECONDS = 300;

    private final WorkTaskRepository workTaskRepository;
    private final WorkerRepository workerRepository;
    private final WifiAccessPointRepository wifiAccessPointRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final StringRedisTemplate redisTemplate;

    public TaskCallService(
            WorkTaskRepository workTaskRepository,
            WorkerRepository workerRepository,
            WifiAccessPointRepository wifiAccessPointRepository,
            TaskAssignmentRepository taskAssignmentRepository,
            StringRedisTemplate redisTemplate
    ) {
        this.workTaskRepository = workTaskRepository;
        this.workerRepository = workerRepository;
        this.wifiAccessPointRepository = wifiAccessPointRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public TaskCallResponse callWorker(Long taskId) {
        WorkTask task = workTaskRepository.findById(taskId)
                .orElseThrow(() -> new TaskCallException("Task not found."));
        StorageLocation taskLocation = resolveTaskLocation(task);
        WifiAccessPoint nearestAccessPoint = findNearestAccessPoint(taskLocation);
        WorkerSignal strongestSignal = findStrongestWorkerSignal(nearestAccessPoint);

        Optional<TaskAssignment> existingAssignment = taskAssignmentRepository.findByTaskIdAndWorkerId(
                task.getId(),
                strongestSignal.worker().getId()
        );
        TaskAssignment assignment = existingAssignment.orElseGet(() -> taskAssignmentRepository.save(new TaskAssignment(
                task,
                strongestSignal.worker(),
                CALLED_STATUS,
                strongestSignal.rssi(),
                LocalDateTime.now(),
                null
        )));
        String alarmKey = createRedisAlarm(strongestSignal.worker(), assignment, task, taskLocation);

        return toResponse(task, taskLocation, nearestAccessPoint, strongestSignal, assignment, alarmKey);
    }

    private StorageLocation resolveTaskLocation(WorkTask task) {
        if (task.getSourceLocation() != null) {
            return task.getSourceLocation();
        }
        if (task.getTargetLocation() != null) {
            return task.getTargetLocation();
        }
        throw new TaskCallException("Task has no source or target location.");
    }

    private WifiAccessPoint findNearestAccessPoint(StorageLocation location) {
        List<WifiAccessPoint> accessPoints = wifiAccessPointRepository.findAll()
                .stream()
                .filter(accessPoint -> Boolean.TRUE.equals(accessPoint.getActive()))
                .filter(accessPoint -> accessPoint.getPosX() != null && accessPoint.getPosY() != null)
                .toList();

        if (accessPoints.isEmpty()) {
            throw new TaskCallException("No active Wi-Fi access points found.");
        }
        if (location.getPosX() == null || location.getPosY() == null) {
            throw new TaskCallException("Task location has no coordinates.");
        }

        return accessPoints.stream()
                .min(Comparator
                        .comparingInt((WifiAccessPoint accessPoint) -> squaredDistance(location, accessPoint))
                        .thenComparing(WifiAccessPoint::getApCode))
                .orElseThrow(() -> new TaskCallException("No active Wi-Fi access points found."));
    }

    private WorkerSignal findStrongestWorkerSignal(WifiAccessPoint accessPoint) {
        List<Worker> workers = workerRepository.findAll();
        if (workers.isEmpty()) {
            throw new TaskCallException("No workers found.");
        }

        String redisField = "ap:" + accessPoint.getApCode();
        return workers.stream()
                .map(worker -> readWorkerSignal(worker, redisField))
                .flatMap(Optional::stream)
                .max(Comparator
                        .comparingInt(WorkerSignal::rssi)
                        .thenComparing(signal -> signal.worker().getId(), Comparator.reverseOrder()))
                .orElseThrow(() -> new TaskCallException("No worker Wi-Fi RSSI found for " + accessPoint.getApCode() + "."));
    }

    private Optional<WorkerSignal> readWorkerSignal(Worker worker, String redisField) {
        String redisKey = "worker:wifi:" + worker.getId();
        Object rawRssi = redisTemplate.opsForHash().get(redisKey, redisField);
        if (rawRssi == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(new WorkerSignal(worker, Integer.parseInt(rawRssi.toString())));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private TaskCallResponse toResponse(
            WorkTask task,
            StorageLocation location,
            WifiAccessPoint accessPoint,
            WorkerSignal signal,
            TaskAssignment assignment,
            String alarmKey
    ) {
        Worker worker = signal.worker();
        return new TaskCallResponse(
                task.getId(),
                assignment.getId(),
                alarmKey,
                worker.getId(),
                worker.getEmployeeNo(),
                worker.getUser().getName(),
                assignment.getStatus(),
                accessPoint.getApCode(),
                signal.rssi(),
                location.getId(),
                location.getCode(),
                location.getPosX(),
                location.getPosY(),
                assignment.getCalledAt()
        );
    }

    private String createRedisAlarm(
            Worker worker,
            TaskAssignment assignment,
            WorkTask task,
            StorageLocation location
    ) {
        String itemName = task.getItem() != null ? task.getItem().getName() : "task";
        String alarmKey = "worker:alarms:" + worker.getId();
        String alarmValue = "TASK_CALL"
                + "|assignmentId=" + assignment.getId()
                + "|taskId=" + task.getId()
                + "|itemName=" + itemName
                + "|locationCode=" + location.getCode()
                + "|calledAt=" + LocalDateTime.now();

        redisTemplate.opsForList().leftPush(alarmKey, alarmValue);
        redisTemplate.opsForList().trim(alarmKey, 0, 19);
        redisTemplate.expire(alarmKey, Duration.ofSeconds(ALARM_TTL_SECONDS));
        return alarmKey;
    }

    private int squaredDistance(StorageLocation location, WifiAccessPoint accessPoint) {
        int diffX = location.getPosX() - accessPoint.getPosX();
        int diffY = location.getPosY() - accessPoint.getPosY();
        return diffX * diffX + diffY * diffY;
    }

    private record WorkerSignal(
            Worker worker,
            int rssi
    ) {
    }
}
