package com.example.logisquare_server.repository;

import com.example.logisquare_server.domain.attendance.AttendanceRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByWorkerIdAndWorkDate(Long workerId, LocalDate workDate);

    List<AttendanceRecord> findAllByWorkerId(Long workerId);
}
