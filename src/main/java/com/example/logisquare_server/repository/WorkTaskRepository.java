package com.example.logisquare_server.repository;

import com.example.logisquare_server.domain.task.WorkTask;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkTaskRepository extends JpaRepository<WorkTask, Long> {

    long countByStatusIn(Collection<String> statuses);

    long countByTaskTypeAndStatusIn(String taskType, Collection<String> statuses);

    List<WorkTask> findTop5ByStatusInOrderByCreatedAtDesc(Collection<String> statuses);

    List<WorkTask> findTop5ByTaskTypeAndStatusInOrderByCreatedAtAsc(String taskType, Collection<String> statuses);

    List<WorkTask> findAllByStatus(String status);

    List<WorkTask> findAllByRequestedById(Long requestedById);
}
