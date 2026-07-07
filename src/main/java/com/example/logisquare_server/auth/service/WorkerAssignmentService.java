package com.example.logisquare_server.auth.service;

import com.example.logisquare_server.auth.dto.WorkerAssignmentResponse;
import com.example.logisquare_server.auth.exception.TaskCallException;
import com.example.logisquare_server.domain.location.StorageLocation;
import com.example.logisquare_server.domain.task.TaskAssignment;
import com.example.logisquare_server.domain.task.WorkTask;
import com.example.logisquare_server.domain.worker.Worker;
import com.example.logisquare_server.repository.TaskAssignmentRepository;
import com.example.logisquare_server.repository.WorkerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WorkerAssignmentService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final WorkerRepository workerRepository;

    public WorkerAssignmentService(
            TaskAssignmentRepository taskAssignmentRepository,
            WorkerRepository workerRepository
    ) {
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.workerRepository = workerRepository;
    }

    public List<WorkerAssignmentResponse> getMyCalls(String authorization, Long workerId) {
        Long resolvedWorkerId = workerId != null ? workerId : resolveWorkerIdFromToken(authorization);
        return getAssignments(resolvedWorkerId);
    }

    public List<WorkerAssignmentResponse> getAssignments(Long workerId) {
        return taskAssignmentRepository.findAllByWorkerId(workerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Long resolveWorkerIdFromToken(String authorization) {
        Long userId = resolveUserId(authorization);
        Worker worker = workerRepository.findByUserId(userId)
                .orElseThrow(() -> new TaskCallException("Worker not found for user."));
        return worker.getId();
    }

    private Long resolveUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new TaskCallException("Authorization bearer token or workerId is required.");
        }

        String token = authorization.substring("Bearer ".length());
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new TaskCallException("Invalid bearer token.");
        }

        try {
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode userId = objectMapper.readTree(payload).get("userId");
            if (userId == null || !userId.canConvertToLong()) {
                throw new TaskCallException("Token has no userId.");
            }
            return userId.asLong();
        } catch (Exception exception) {
            throw new TaskCallException("Invalid bearer token.");
        }
    }

    private WorkerAssignmentResponse toResponse(TaskAssignment assignment) {
        WorkTask task = assignment.getTask();
        StorageLocation sourceLocation = task.getSourceLocation();
        StorageLocation targetLocation = task.getTargetLocation();

        return new WorkerAssignmentResponse(
                assignment.getId(),
                task.getId(),
                assignment.getStatus(),
                task.getTaskType(),
                task.getItem() != null ? task.getItem().getId() : null,
                task.getItem() != null ? task.getItem().getName() : null,
                task.getQuantity(),
                sourceLocation != null ? sourceLocation.getId() : null,
                sourceLocation != null ? sourceLocation.getCode() : null,
                targetLocation != null ? targetLocation.getId() : null,
                targetLocation != null ? targetLocation.getCode() : null,
                assignment.getCalledAt()
        );
    }
}
