package com.example.logisquare_server.repository;

import com.example.logisquare_server.domain.safety.SafetyEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SafetyEventRepository extends JpaRepository<SafetyEvent, Long> {

    List<SafetyEvent> findAllByStatus(String status);

    List<SafetyEvent> findAllByWorkerId(Long workerId);

    List<SafetyEvent> findAllByStorageLocationId(Long storageLocationId);
}
