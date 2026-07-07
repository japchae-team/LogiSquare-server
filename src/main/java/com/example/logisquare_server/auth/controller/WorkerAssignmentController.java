package com.example.logisquare_server.auth.controller;

import com.example.logisquare_server.auth.dto.WorkerAssignmentResponse;
import com.example.logisquare_server.auth.service.WorkerAssignmentService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workers")
public class WorkerAssignmentController {

    private final WorkerAssignmentService workerAssignmentService;

    public WorkerAssignmentController(WorkerAssignmentService workerAssignmentService) {
        this.workerAssignmentService = workerAssignmentService;
    }

    @GetMapping("/{workerId}/assignments")
    public ResponseEntity<List<WorkerAssignmentResponse>> getAssignments(@PathVariable Long workerId) {
        return ResponseEntity.ok(workerAssignmentService.getAssignments(workerId));
    }
}
