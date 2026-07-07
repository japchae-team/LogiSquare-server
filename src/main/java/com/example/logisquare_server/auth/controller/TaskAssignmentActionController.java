package com.example.logisquare_server.auth.controller;

import com.example.logisquare_server.auth.dto.TaskActionResponse;
import com.example.logisquare_server.auth.service.TaskAssignmentActionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class TaskAssignmentActionController {

    private final TaskAssignmentActionService taskAssignmentActionService;

    public TaskAssignmentActionController(TaskAssignmentActionService taskAssignmentActionService) {
        this.taskAssignmentActionService = taskAssignmentActionService;
    }

    @PatchMapping("/api/task-assignments/{assignmentId}/accept")
    public ResponseEntity<TaskActionResponse> accept(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(taskAssignmentActionService.accept(assignmentId));
    }

    @PatchMapping("/api/task-assignments/{assignmentId}/reject")
    public ResponseEntity<TaskActionResponse> reject(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(taskAssignmentActionService.reject(assignmentId));
    }

    @PatchMapping("/api/tasks/{taskId}/complete")
    public ResponseEntity<TaskActionResponse> complete(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskAssignmentActionService.complete(taskId));
    }
}
