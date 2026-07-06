package com.example.logisquare_server.repository;

import com.example.logisquare_server.domain.location.StorageLocation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long> {

    Optional<StorageLocation> findByCode(String code);

    boolean existsByCode(String code);
}
