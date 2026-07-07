package com.example.logisquare_server.auth.service;

import com.example.logisquare_server.auth.dto.TaskActionResponse;
import com.example.logisquare_server.auth.exception.TaskCallException;
import com.example.logisquare_server.domain.inventory.Inventory;
import com.example.logisquare_server.domain.location.StorageLocation;
import com.example.logisquare_server.domain.task.TaskAssignment;
import com.example.logisquare_server.domain.task.WorkTask;
import com.example.logisquare_server.repository.InventoryRepository;
import com.example.logisquare_server.repository.TaskAssignmentRepository;
import com.example.logisquare_server.repository.WorkTaskRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskAssignmentActionService {

    private final TaskAssignmentRepository taskAssignmentRepository;
    private final WorkTaskRepository workTaskRepository;
    private final InventoryRepository inventoryRepository;

    public TaskAssignmentActionService(
            TaskAssignmentRepository taskAssignmentRepository,
            WorkTaskRepository workTaskRepository,
            InventoryRepository inventoryRepository
    ) {
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.workTaskRepository = workTaskRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public TaskActionResponse accept(Long assignmentId) {
        TaskAssignment assignment = findAssignment(assignmentId);
        LocalDateTime now = LocalDateTime.now();

        assignment.accept(now);
        assignment.getTask().markInProgress(now);

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
        reflectInventory(task, now);

        return toResponse(assignment, assignment.getRespondedAt(), now);
    }

    private TaskAssignment findAssignment(Long assignmentId) {
        return taskAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new TaskCallException("Assignment not found."));
    }

    private void validateCompletableTask(WorkTask task) {
        if (task.getItem() == null) {
            throw new TaskCallException("Task has no item.");
        }
        if (task.getTargetLocation() == null) {
            throw new TaskCallException("Task has no target location.");
        }
        if (task.getQuantity() == null || task.getQuantity() <= 0) {
            throw new TaskCallException("Task quantity must be greater than 0.");
        }
    }

    private void reflectInventory(WorkTask task, LocalDateTime movedAt) {
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
