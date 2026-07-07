package com.example.logisquare_server.dashboard.service;

import com.example.logisquare_server.dashboard.dto.DashboardSummaryResponse;
import com.example.logisquare_server.repository.InboundRecordRepository;
import com.example.logisquare_server.repository.SafetyEventRepository;
import com.example.logisquare_server.repository.WorkTaskRepository;
import com.example.logisquare_server.repository.WorkerRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final List<String> IN_PROGRESS_TASK_STATUSES = List.of(
            "IN_PROGRESS", "PROCESSING", "ACCEPTED", "진행중", "진행 중"
    );
    private static final List<String> AVAILABLE_WORKER_STATUSES = List.of(
            "AVAILABLE", "ACTIVE", "IDLE", "가용", "대기"
    );
    private static final List<String> SAFETY_VIOLATION_STATUSES = List.of(
            "OPEN", "DETECTED", "ASSIGNED", "UNRESOLVED", "VIOLATION", "미해결", "위반"
    );
    private static final List<String> PENDING_INBOUND_STATUSES = List.of(
            "PENDING", "WAITING", "REQUESTED", "입고대기", "입고 대기"
    );

    private final WorkTaskRepository workTaskRepository;
    private final WorkerRepository workerRepository;
    private final SafetyEventRepository safetyEventRepository;
    private final InboundRecordRepository inboundRecordRepository;

    public DashboardService(
            WorkTaskRepository workTaskRepository,
            WorkerRepository workerRepository,
            SafetyEventRepository safetyEventRepository,
            InboundRecordRepository inboundRecordRepository
    ) {
        this.workTaskRepository = workTaskRepository;
        this.workerRepository = workerRepository;
        this.safetyEventRepository = safetyEventRepository;
        this.inboundRecordRepository = inboundRecordRepository;
    }

    public DashboardSummaryResponse getSummary() {
        return new DashboardSummaryResponse(
                workTaskRepository.countByStatusIn(IN_PROGRESS_TASK_STATUSES),
                workerRepository.countByStatusIn(AVAILABLE_WORKER_STATUSES),
                safetyEventRepository.countByStatusIn(SAFETY_VIOLATION_STATUSES),
                inboundRecordRepository.countByStatusIn(PENDING_INBOUND_STATUSES)
        );
    }
}
