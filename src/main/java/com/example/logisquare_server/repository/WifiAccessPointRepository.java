package com.example.logisquare_server.repository;

import com.example.logisquare_server.domain.device.WifiAccessPoint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WifiAccessPointRepository extends JpaRepository<WifiAccessPoint, Long> {

    Optional<WifiAccessPoint> findByApCode(String apCode);

    Optional<WifiAccessPoint> findByBssid(String bssid);
}
