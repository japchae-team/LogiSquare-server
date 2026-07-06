package com.example.logisquare_server.repository;

import com.example.logisquare_server.domain.task.TaskAssignment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

    List<TaskAssignment> findAllByTaskId(Long taskId);

    List<TaskAssignment> findAllByWorkerId(Long workerId);

    Optional<TaskAssignment> findByTaskIdAndWorkerId(Long taskId, Long workerId);
}
