package com.example.logisquare_server.repository;

import com.example.logisquare_server.domain.inventory.Inventory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByItemIdAndStorageLocationId(Long itemId, Long storageLocationId);

    List<Inventory> findAllByItemId(Long itemId);

    List<Inventory> findAllByStorageLocationId(Long storageLocationId);
}
