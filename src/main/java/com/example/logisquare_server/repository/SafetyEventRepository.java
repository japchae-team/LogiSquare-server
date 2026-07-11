package com.example.logisquare_server.repository;

import com.example.logisquare_server.domain.safety.SafetyEvent;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SafetyEventRepository extends JpaRepository<SafetyEvent, Long> {

    long countByStatusIn(Collection<String> statuses);

    List<SafetyEvent> findTop5ByStatusInOrderByOccurredAtDesc(Collection<String> statuses);

    List<SafetyEvent> findAllByOrderByOccurredAtDesc();

    List<SafetyEvent> findAllByStatusOrderByOccurredAtDesc(String status);

    List<SafetyEvent> findAllByEventTypeOrderByOccurredAtDesc(String eventType);

    List<SafetyEvent> findAllByEventTypeAndStatusOrderByOccurredAtDesc(String eventType, String status);

    List<SafetyEvent> findAllByStatus(String status);

    List<SafetyEvent> findAllByWorkerId(Long workerId);

    List<SafetyEvent> findAllByStorageLocationId(Long storageLocationId);
}
