package com.example.logisquare_server.auth.controller;

import com.example.logisquare_server.auth.dto.TaskCallResponse;
import com.example.logisquare_server.auth.service.TaskCallService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskCallController {

    private final TaskCallService taskCallService;

    public TaskCallController(TaskCallService taskCallService) {
        this.taskCallService = taskCallService;
    }

    @PostMapping("/{taskId}/call")
    public ResponseEntity<TaskCallResponse> callWorker(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskCallService.callWorker(taskId));
    }
}
