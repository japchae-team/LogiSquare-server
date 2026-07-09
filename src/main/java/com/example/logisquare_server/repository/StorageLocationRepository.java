package com.example.logisquare_server.repository;

import com.example.logisquare_server.domain.location.StorageLocation;
import com.example.logisquare_server.domain.location.StorageGrade;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long> {

    @Query("""
            select location
            from StorageLocation location
            where location.active = true
              and location.locationGrade = :locationGrade
              and location.capacity is not null
              and (
                  select coalesce(sum(inventory.quantity), 0)
                  from Inventory inventory
                  where inventory.storageLocation = location
                    and inventory.quantity > 0
              ) <= location.capacity - :quantity
            order by location.posX asc, location.posY asc, location.code asc
            limit 1
            """)
    Optional<StorageLocation> findFirstAvailableLocationByGrade(
            @Param("locationGrade") StorageGrade locationGrade,
            @Param("quantity") Integer quantity
    );

    List<StorageLocation> findAllByActiveTrueOrderByPosYDescPosXAscCodeAsc();

    Optional<StorageLocation> findByCode(String code);

    Optional<StorageLocation> findFirstByActiveTrueOrderByIdAsc();

    boolean existsByCode(String code);
}
