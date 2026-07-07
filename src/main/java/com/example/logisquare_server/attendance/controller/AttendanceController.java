package com.example.logisquare_server.attendance.controller;

import com.example.logisquare_server.attendance.dto.WorkerAttendanceStatsResponse;
import com.example.logisquare_server.attendance.service.AttendanceService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/workers/stats")
    public ResponseEntity<List<WorkerAttendanceStatsResponse>> getWorkerStats(
            @RequestParam(defaultValue = "일") String period
    ) {
        return ResponseEntity.ok(attendanceService.getWorkerStats(period));
    }
}
