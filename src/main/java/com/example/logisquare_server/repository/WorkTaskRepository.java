package com.example.logisquare_server.repository;

import com.example.logisquare_server.domain.task.WorkTask;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkTaskRepository extends JpaRepository<WorkTask, Long> {

    List<WorkTask> findAllByStatus(String status);

    List<WorkTask> findAllByRequestedById(Long requestedById);
}
