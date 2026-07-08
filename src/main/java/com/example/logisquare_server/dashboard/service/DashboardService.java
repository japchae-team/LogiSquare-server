package com.example.logisquare_server.dashboard.service;

import com.example.logisquare_server.dashboard.dto.DashboardInboundItemResponse;
import com.example.logisquare_server.dashboard.dto.DashboardSafetyItemResponse;
import com.example.logisquare_server.dashboard.dto.DashboardSummaryResponse;
import com.example.logisquare_server.dashboard.dto.DashboardTaskItemResponse;
import com.example.logisquare_server.dashboard.dto.DashboardWorkerItemResponse;
import com.example.logisquare_server.domain.safety.SafetyEvent;
import com.example.logisquare_server.domain.task.WorkTask;
import com.example.logisquare_server.domain.worker.Worker;
import com.example.logisquare_server.repository.SafetyEventRepository;
import com.example.logisquare_server.repository.WorkTaskRepository;
import com.example.logisquare_server.repository.WorkerRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final String INBOUND_TASK_TYPE = "INBOUND";
    private static final String ACCEPTED_ASSIGNMENT_STATUS = "ACCEPTED";
    private static final List<String> IN_PROGRESS_TASK_STATUSES = List.of("IN_PROGRESS", "PROCESSING", "ACCEPTED");
    private static final List<String> AVAILABLE_WORKER_STATUSES = List.of("AVAILABLE", "ACTIVE", "IDLE");
    private static final List<String> SAFETY_VIOLATION_STATUSES = List.of(
            "OPEN", "DETECTED", "ASSIGNED", "UNRESOLVED", "VIOLATION"
    );
    private static final List<String> PENDING_INBOUND_STATUSES = List.of("PENDING", "WAITING", "REQUESTED");

    private final WorkTaskRepository workTaskRepository;
    private final WorkerRepository workerRepository;
    private final SafetyEventRepository safetyEventRepository;

    public DashboardService(
            WorkTaskRepository workTaskRepository,
            WorkerRepository workerRepository,
            SafetyEventRepository safetyEventRepository
    ) {
        this.workTaskRepository = workTaskRepository;
        this.workerRepository = workerRepository;
        this.safetyEventRepository = safetyEventRepository;
    }

    public DashboardSummaryResponse getSummary() {
        return new DashboardSummaryResponse(
                workTaskRepository.countByStatusIn(IN_PROGRESS_TASK_STATUSES),
                workerRepository.countByStatusIn(AVAILABLE_WORKER_STATUSES),
                safetyEventRepository.countByStatusIn(SAFETY_VIOLATION_STATUSES),
                workTaskRepository.countPendingTasksWithoutAcceptedAssignment(
                        INBOUND_TASK_TYPE,
                        PENDING_INBOUND_STATUSES,
                        ACCEPTED_ASSIGNMENT_STATUS
                ),
                getInProgressTasks(),
                getAvailableWorkers(),
                getSafetyViolations(),
                getPendingInbounds()
        );
    }

    private List<DashboardTaskItemResponse> getInProgressTasks() {
        return workTaskRepository.findTop5ByStatusInOrderByCreatedAtDesc(IN_PROGRESS_TASK_STATUSES)
                .stream()
                .map(task -> new DashboardTaskItemResponse(
                        task.getId(),
                        task.getItem() != null ? task.getItem().getName() : null,
                        task.getQuantity(),
                        task.getStatus()
                ))
                .toList();
    }

    private List<DashboardWorkerItemResponse> getAvailableWorkers() {
        return workerRepository.findTop5ByStatusInOrderByEmployeeNoAsc(AVAILABLE_WORKER_STATUSES)
                .stream()
                .map(worker -> new DashboardWorkerItemResponse(
                        worker.getId(),
                        worker.getEmployeeNo(),
                        worker.getUser().getName(),
                        worker.getStatus()
                ))
                .toList();
    }

    private List<DashboardSafetyItemResponse> getSafetyViolations() {
        return safetyEventRepository.findTop5ByStatusInOrderByOccurredAtDesc(SAFETY_VIOLATION_STATUSES)
                .stream()
                .map(this::toSafetyItem)
                .toList();
    }

    private DashboardSafetyItemResponse toSafetyItem(SafetyEvent event) {
        return new DashboardSafetyItemResponse(
                event.getId(),
                event.getEventType(),
                event.getStorageLocation().getCode(),
                event.getWorker() != null ? event.getWorker().getUser().getName() : null,
                event.getOccurredAt()
        );
    }

    private List<DashboardInboundItemResponse> getPendingInbounds() {
        return workTaskRepository
                .findPendingTasksWithoutAcceptedAssignmentOrderByCreatedAtAsc(
                        INBOUND_TASK_TYPE,
                        PENDING_INBOUND_STATUSES,
                        ACCEPTED_ASSIGNMENT_STATUS
                )
                .stream()
                .map(this::toInboundItem)
                .toList();
    }

    private DashboardInboundItemResponse toInboundItem(WorkTask task) {
        return new DashboardInboundItemResponse(
                task.getId(),
                task.getItem() != null ? task.getItem().getId() : null,
                task.getItem() != null ? task.getItem().getName() : null,
                task.getQuantity(),
                task.getTargetLocation() != null ? task.getTargetLocation().getCode() : null,
                task.getCreatedAt()
        );
    }
}
