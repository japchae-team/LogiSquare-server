package com.example.logisquare_server.attendance.dto;

public record WorkerAttendanceStatsResponse(
        Long workerId,
        String name,
        String status,
        String checkIn,
        String checkOut,
        long callAccepted,
        long tasksHandled,
        long violations
) {
}
