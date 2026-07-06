package com.example.logisquare_server.repository;

import com.example.logisquare_server.domain.inbound.InboundRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboundRecordRepository extends JpaRepository<InboundRecord, Long> {

    List<InboundRecord> findAllByStatus(String status);

    List<InboundRecord> findAllByTargetLocationId(Long targetLocationId);
}
