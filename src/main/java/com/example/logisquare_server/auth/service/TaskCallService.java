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
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskCallService {

    private static final String CALLED_STATUS = "CALLED";
    private static final String INBOUND_TASK_TYPE = "INBOUND";
    private static final String OUTBOUND_TASK_TYPE = "OUTBOUND";
    private static final List<String> AVAILABLE_WORKER_STATUSES = List.of("AVAILABLE", "ACTIVE", "IDLE");
    private static final List<String> ACTIVE_ASSIGNMENT_STATUSES = List.of("CALLED", "ACCEPTED");
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
    public TaskCallResponse callOutboundWorker(Long taskId) {
        WorkTask task = findTask(taskId);
        validateTaskType(task, OUTBOUND_TASK_TYPE);
        StorageLocation taskLocation = resolveTaskLocation(task);
        WifiAccessPoint nearestAccessPoint = findNearestAccessPoint(taskLocation);

        Optional<TaskAssignment> activeAssignment = findActiveTaskAssignment(task);
        if (activeAssignment.isPresent()) {
            TaskAssignment assignment = activeAssignment.get();
            return toResponse(
                    task,
                    taskLocation,
                    nearestAccessPoint,
                    assignment.getWorker(),
                    assignment.getDistanceScore(),
                    assignment,
                    "worker:alarms:" + assignment.getWorker().getId()
            );
        }

        WorkerSignal strongestSignal = findStrongestWorkerSignal(nearestAccessPoint);
        TaskAssignment assignment = createAssignment(task, strongestSignal.worker(), strongestSignal.rssi());
        String alarmKey = createRedisAlarm(strongestSignal.worker(), assignment, task, taskLocation);

        return toResponse(
                task,
                taskLocation,
                nearestAccessPoint,
                strongestSignal.worker(),
                strongestSignal.rssi(),
                assignment,
                alarmKey
        );
    }

    @Transactional
    public List<TaskCallResponse> callInboundWorkers(Long taskId) {
        WorkTask task = findTask(taskId);
        validateTaskType(task, INBOUND_TASK_TYPE);
        StorageLocation taskLocation = resolveTaskLocation(task);

        List<TaskAssignment> activeAssignments = findActiveTaskAssignments(task);
        if (!activeAssignments.isEmpty()) {
            return activeAssignments.stream()
                    .map(assignment -> toResponse(
                            task,
                            taskLocation,
                            null,
                            assignment.getWorker(),
                            assignment.getDistanceScore(),
                            assignment,
                            "worker:alarms:" + assignment.getWorker().getId()
                    ))
                    .toList();
        }

        List<Worker> workers = findAvailableWorkers();
        if (workers.isEmpty()) {
            throw new TaskCallException("No available workers found.");
        }

        return workers.stream()
                .map(worker -> {
                    TaskAssignment assignment = createAssignment(task, worker, null);
                    String alarmKey = createRedisAlarm(worker, assignment, task, taskLocation);
                    return toResponse(task, taskLocation, null, worker, null, assignment, alarmKey);
                })
                .toList();
    }

    private WorkTask findTask(Long taskId) {
        return workTaskRepository.findById(taskId)
                .orElseThrow(() -> new TaskCallException("Task not found."));
    }

    private void validateTaskType(WorkTask task, String taskType) {
        if (!taskType.equals(task.getTaskType())) {
            throw new TaskCallException("Task is not " + taskType + ".");
        }
    }

    private Optional<TaskAssignment> findActiveTaskAssignment(WorkTask task) {
        return taskAssignmentRepository.findFirstByTaskIdAndStatusInOrderByCalledAtDesc(
                task.getId(),
                ACTIVE_ASSIGNMENT_STATUSES
        );
    }

    private List<TaskAssignment> findActiveTaskAssignments(WorkTask task) {
        return taskAssignmentRepository.findAllByTaskIdAndStatusIn(
                task.getId(),
                ACTIVE_ASSIGNMENT_STATUSES
        );
    }

    private TaskAssignment createAssignment(WorkTask task, Worker worker, Integer distanceScore) {
        return taskAssignmentRepository.save(new TaskAssignment(
                task,
                worker,
                CALLED_STATUS,
                distanceScore,
                LocalDateTime.now(),
                null
        ));
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
        List<Worker> workers = findAvailableWorkers();
        if (workers.isEmpty()) {
            throw new TaskCallException("No available workers found.");
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

    private List<Worker> findAvailableWorkers() {
        return workerRepository.findAllByStatusIn(AVAILABLE_WORKER_STATUSES)
                .stream()
                .filter(worker -> !hasActiveAssignment(worker.getId(), ACTIVE_ASSIGNMENT_STATUSES))
                .toList();
    }

    private boolean hasActiveAssignment(Long workerId, Collection<String> statuses) {
        return taskAssignmentRepository.existsByWorkerIdAndStatusIn(workerId, statuses);
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
            Worker worker,
            Integer rssi,
            TaskAssignment assignment,
            String alarmKey
    ) {
        return new TaskCallResponse(
                task.getId(),
                assignment.getId(),
                alarmKey,
                worker.getId(),
                worker.getEmployeeNo(),
                worker.getUser().getName(),
                assignment.getStatus(),
                accessPoint != null ? accessPoint.getApCode() : null,
                rssi,
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
