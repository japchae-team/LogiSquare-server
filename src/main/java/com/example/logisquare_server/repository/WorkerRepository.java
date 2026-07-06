package com.example.logisquare_server.repository;

import com.example.logisquare_server.domain.worker.Worker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerRepository extends JpaRepository<Worker, Long> {

    Optional<Worker> findByEmployeeNo(String employeeNo);

    boolean existsByEmployeeNo(String employeeNo);
}
