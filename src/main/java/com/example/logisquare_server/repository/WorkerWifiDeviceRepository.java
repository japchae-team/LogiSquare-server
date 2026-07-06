package com.example.logisquare_server.repository;

import com.example.logisquare_server.domain.device.WorkerWifiDevice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerWifiDeviceRepository extends JpaRepository<WorkerWifiDevice, Long> {

    Optional<WorkerWifiDevice> findByDeviceIdentifier(String deviceIdentifier);

    List<WorkerWifiDevice> findAllByWorkerId(Long workerId);
}
