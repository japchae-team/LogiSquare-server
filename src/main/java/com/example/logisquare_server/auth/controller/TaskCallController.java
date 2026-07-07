package com.example.logisquare_server.auth.controller;

import com.example.logisquare_server.auth.dto.TaskCallResponse;
import com.example.logisquare_server.auth.service.TaskCallService;
import com.example.logisquare_server.auth.dto.WorkerAssignmentResponse;
import com.example.logisquare_server.auth.service.WorkerAssignmentService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskCallController {

    private final TaskCallService taskCallService;
    private final WorkerAssignmentService workerAssignmentService;

    public TaskCallController(
            TaskCallService taskCallService,
            WorkerAssignmentService workerAssignmentService
    ) {
        this.taskCallService = taskCallService;
        this.workerAssignmentService = workerAssignmentService;
    }

    @GetMapping("/my-calls")
    public ResponseEntity<List<WorkerAssignmentResponse>> getMyCalls(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) Long workerId
    ) {
        return ResponseEntity.ok(workerAssignmentService.getMyCalls(authorization, workerId));
    }

    @PostMapping("/{taskId}/call")
    public ResponseEntity<TaskCallResponse> callWorker(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskCallService.callWorker(taskId));
    }
}
