package com.example.logisquare_server.repository;

import com.example.logisquare_server.domain.worker.Worker;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerRepository extends JpaRepository<Worker, Long> {

    long countByStatusIn(Collection<String> statuses);

    List<Worker> findTop5ByStatusInOrderByEmployeeNoAsc(Collection<String> statuses);

    Optional<Worker> findByUserId(Long userId);

    Optional<Worker> findByEmployeeNo(String employeeNo);

    boolean existsByEmployeeNo(String employeeNo);
}
