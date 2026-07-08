package com.example.logisquare_server.auth.service;

import com.example.logisquare_server.auth.dto.TaskActionResponse;
import com.example.logisquare_server.auth.exception.TaskCallException;
import com.example.logisquare_server.domain.inbound.InboundRecord;
import com.example.logisquare_server.domain.inventory.Inventory;
import com.example.logisquare_server.domain.location.StorageLocation;
import com.example.logisquare_server.domain.task.TaskAssignment;
import com.example.logisquare_server.domain.task.WorkTask;
import com.example.logisquare_server.repository.InboundRecordRepository;
import com.example.logisquare_server.repository.InventoryRepository;
import com.example.logisquare_server.repository.TaskAssignmentRepository;
import com.example.logisquare_server.repository.WorkTaskRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskAssignmentActionService {

    private static final String CALLED_ASSIGNMENT_STATUS = "CALLED";
    private static final String ACCEPTED_ASSIGNMENT_STATUS = "ACCEPTED";
    private static final String COMPLETED_INBOUND_STATUS = "COMPLETED";
    private static final String INBOUND_TASK_TYPE = "INBOUND";
    private static final String OUTBOUND_TASK_TYPE = "OUTBOUND";

    private final TaskAssignmentRepository taskAssignmentRepository;
    private final WorkTaskRepository workTaskRepository;
    private final InventoryRepository inventoryRepository;
    private final InboundRecordRepository inboundRecordRepository;

    public TaskAssignmentActionService(
            TaskAssignmentRepository taskAssignmentRepository,
            WorkTaskRepository workTaskRepository,
            InventoryRepository inventoryRepository,
            InboundRecordRepository inboundRecordRepository
    ) {
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.workTaskRepository = workTaskRepository;
        this.inventoryRepository = inventoryRepository;
        this.inboundRecordRepository = inboundRecordRepository;
    }

    @Transactional
    public TaskActionResponse accept(Long assignmentId) {
        TaskAssignment assignment = findAssignment(assignmentId);
        List<TaskAssignment> taskAssignments = taskAssignmentRepository.findAllByTaskIdForUpdate(
                assignment.getTask().getId()
        );
        validateAcceptableAssignment(assignment);

        LocalDateTime now = LocalDateTime.now();

        assignment.accept(now);
        assignment.getTask().markInProgress(now);
        assignment.getWorker().markWorking();
        cancelOtherCalledAssignments(taskAssignments, assignment, now);

        return toResponse(assignment, now, null);
    }

    @Transactional
    public TaskActionResponse reject(Long assignmentId) {
        TaskAssignment assignment = findAssignment(assignmentId);
        LocalDateTime now = LocalDateTime.now();

        assignment.reject(now);

        return toResponse(assignment, now, null);
    }

    @Transactional
    public TaskActionResponse complete(Long taskId) {
        WorkTask task = workTaskRepository.findById(taskId)
                .orElseThrow(() -> new TaskCallException("Task not found."));
        TaskAssignment assignment = taskAssignmentRepository.findAllByTaskId(taskId)
                .stream()
                .filter(foundAssignment -> "ACCEPTED".equals(foundAssignment.getStatus()))
                .findFirst()
                .orElseThrow(() -> new TaskCallException("Accepted assignment not found."));

        validateCompletableTask(task);

        LocalDateTime now = LocalDateTime.now();
        task.complete(now);
        assignment.complete(now);
        assignment.getWorker().markAvailable();
        reflectInventory(task, assignment, now);

        return toResponse(assignment, assignment.getRespondedAt(), now);
    }

    private TaskAssignment findAssignment(Long assignmentId) {
        return taskAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new TaskCallException("Assignment not found."));
    }

    private void validateAcceptableAssignment(TaskAssignment assignment) {
        if (!CALLED_ASSIGNMENT_STATUS.equals(assignment.getStatus())) {
            throw new TaskCallException("Assignment is not callable.");
        }
        if (taskAssignmentRepository.existsByTaskIdAndStatus(
                assignment.getTask().getId(),
                ACCEPTED_ASSIGNMENT_STATUS
        )) {
            throw new TaskCallException("Task has already been accepted.");
        }
    }

    private void cancelOtherCalledAssignments(
            List<TaskAssignment> taskAssignments,
            TaskAssignment acceptedAssignment,
            LocalDateTime now
    ) {
        taskAssignments.stream()
                .filter(assignment -> !assignment.getId().equals(acceptedAssignment.getId()))
                .filter(assignment -> CALLED_ASSIGNMENT_STATUS.equals(assignment.getStatus()))
                .forEach(assignment -> assignment.cancel(now));
    }

    private void validateCompletableTask(WorkTask task) {
        if (task.getItem() == null) {
            throw new TaskCallException("Task has no item.");
        }
        if (task.getQuantity() == null || task.getQuantity() <= 0) {
            throw new TaskCallException("Task quantity must be greater than 0.");
        }
        if (INBOUND_TASK_TYPE.equals(task.getTaskType()) && task.getTargetLocation() == null) {
            throw new TaskCallException("Inbound task has no target location.");
        }
        if (OUTBOUND_TASK_TYPE.equals(task.getTaskType()) && task.getSourceLocation() == null) {
            throw new TaskCallException("Outbound task has no source location.");
        }
        if (!INBOUND_TASK_TYPE.equals(task.getTaskType()) && !OUTBOUND_TASK_TYPE.equals(task.getTaskType())) {
            throw new TaskCallException("Unsupported task type.");
        }
    }

    private void reflectInventory(WorkTask task, TaskAssignment assignment, LocalDateTime movedAt) {
        if (INBOUND_TASK_TYPE.equals(task.getTaskType())) {
            increaseInventory(task, movedAt);
            createInboundRecord(task, assignment, movedAt);
            return;
        }

        decreaseInventory(task, movedAt);
    }

    private void increaseInventory(WorkTask task, LocalDateTime movedAt) {
        StorageLocation targetLocation = task.getTargetLocation();
        Inventory inventory = inventoryRepository
                .findByItemIdAndStorageLocationId(task.getItem().getId(), targetLocation.getId())
                .orElseGet(() -> inventoryRepository.save(new Inventory(
                        task.getItem(),
                        targetLocation,
                        0,
                        movedAt
                )));

        inventory.addQuantity(task.getQuantity(), movedAt);
    }

    private void decreaseInventory(WorkTask task, LocalDateTime movedAt) {
        StorageLocation sourceLocation = task.getSourceLocation();
        Inventory inventory = inventoryRepository
                .findByItemIdAndStorageLocationId(task.getItem().getId(), sourceLocation.getId())
                .orElseThrow(() -> new TaskCallException("Inventory not found at source location."));

        if (inventory.getQuantity() < task.getQuantity()) {
            throw new TaskCallException("Not enough inventory at source location.");
        }

        inventory.subtractQuantity(task.getQuantity(), movedAt);
    }

    private void createInboundRecord(WorkTask task, TaskAssignment assignment, LocalDateTime receivedAt) {
        inboundRecordRepository.save(new InboundRecord(
                task.getItem(),
                task.getQuantity(),
                task.getTargetLocation(),
                assignment.getWorker().getUser(),
                COMPLETED_INBOUND_STATUS,
                receivedAt
        ));
    }

    private TaskActionResponse toResponse(
            TaskAssignment assignment,
            LocalDateTime respondedAt,
            LocalDateTime completedAt
    ) {
        WorkTask task = assignment.getTask();
        return new TaskActionResponse(
                task.getId(),
                assignment.getId(),
                assignment.getWorker().getId(),
                task.getStatus(),
                assignment.getStatus(),
                respondedAt,
                completedAt
        );
    }
}
