package com.example.logisquare_server.attendance.service;

import com.example.logisquare_server.attendance.dto.WorkerAttendanceStatsResponse;
import com.example.logisquare_server.domain.attendance.AttendanceRecord;
import com.example.logisquare_server.domain.worker.Worker;
import com.example.logisquare_server.repository.AttendanceRecordRepository;
import com.example.logisquare_server.repository.SafetyEventRepository;
import com.example.logisquare_server.repository.TaskAssignmentRepository;
import com.example.logisquare_server.repository.WorkerRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AttendanceService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    // 기간 파라미터는 한글/영문 표기를 모두 허용한다 (프론트 표기와 백엔드 관용 표기가 다를 수 있어서)
    private static final Map<String, Long> PERIOD_DAYS = Map.of(
            "일", 1L, "DAY", 1L,
            "주", 7L, "WEEK", 7L,
            "월", 30L, "MONTH", 30L,
            "년", 365L, "YEAR", 365L
    );

    private final WorkerRepository workerRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final SafetyEventRepository safetyEventRepository;

    public AttendanceService(
            WorkerRepository workerRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            TaskAssignmentRepository taskAssignmentRepository,
            SafetyEventRepository safetyEventRepository
    ) {
        this.workerRepository = workerRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.safetyEventRepository = safetyEventRepository;
    }

    public List<WorkerAttendanceStatsResponse> getWorkerStats(String period) {
        long days = PERIOD_DAYS.getOrDefault(normalize(period), 1L);
        LocalDate today = LocalDate.now();
        LocalDateTime rangeStart = today.minusDays(days - 1).atStartOfDay();
        LocalDateTime rangeEnd = today.plusDays(1).atStartOfDay();

        return workerRepository.findAll().stream()
                .map(worker -> toResponse(worker, today, rangeStart, rangeEnd))
                .toList();
    }

    private String normalize(String period) {
        return period == null ? "" : period.trim().toUpperCase();
    }

    private WorkerAttendanceStatsResponse toResponse(
            Worker worker, LocalDate today, LocalDateTime rangeStart, LocalDateTime rangeEnd
    ) {
        AttendanceRecord todayRecord = attendanceRecordRepository
                .findByWorkerIdAndWorkDate(worker.getId(), today)
                .orElse(null);

        long callAccepted = taskAssignmentRepository.findAllByWorkerId(worker.getId()).stream()
                .filter(a -> a.getRespondedAt() != null && isWithin(a.getRespondedAt(), rangeStart, rangeEnd))
                .count();

        long tasksHandled = taskAssignmentRepository.findAllByWorkerId(worker.getId()).stream()
                .filter(a -> a.getTask().getCompletedAt() != null && isWithin(a.getTask().getCompletedAt(), rangeStart, rangeEnd))
                .count();

        long violations = safetyEventRepository.findAllByWorkerId(worker.getId()).stream()
                .filter(e -> isWithin(e.getOccurredAt(), rangeStart, rangeEnd))
                .count();

        return new WorkerAttendanceStatsResponse(
                worker.getId(),
                worker.getUser().getName(),
                worker.getStatus(),
                formatTime(todayRecord == null ? null : todayRecord.getClockInAt()),
                formatTime(todayRecord == null ? null : todayRecord.getClockOutAt()),
                callAccepted,
                tasksHandled,
                violations
        );
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? "-" : value.format(TIME_FORMAT);
    }

    private boolean isWithin(LocalDateTime value, LocalDateTime start, LocalDateTime end) {
        return !value.isBefore(start) && value.isBefore(end);
    }
}
