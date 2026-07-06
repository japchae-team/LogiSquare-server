package com.example.logisquare_server.repository;

import com.example.logisquare_server.domain.device.CctvCamera;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CctvCameraRepository extends JpaRepository<CctvCamera, Long> {

    Optional<CctvCamera> findByCameraCode(String cameraCode);

    List<CctvCamera> findAllByStorageLocationId(Long storageLocationId);
}
