package com.example.logisquare_server.domain.inventory;

import com.example.logisquare_server.domain.common.BaseTimeEntity;
import com.example.logisquare_server.domain.item.Item;
import com.example.logisquare_server.domain.location.StorageLocation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "inventories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventories_item_location",
                        columnNames = {"item_id", "storage_location_id"}
                )
        },
        indexes = {
                @Index(name = "idx_inventories_item_id", columnList = "item_id"),
                @Index(name = "idx_inventories_storage_location_id", columnList = "storage_location_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "storage_location_id", nullable = false)
    private StorageLocation storageLocation;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "last_moved_at")
    private LocalDateTime lastMovedAt;

    public Inventory(Item item, StorageLocation storageLocation, Integer quantity, LocalDateTime lastMovedAt) {
        this.item = item;
        this.storageLocation = storageLocation;
        this.quantity = quantity;
        this.lastMovedAt = lastMovedAt;
    }

    public void addQuantity(Integer quantity, LocalDateTime movedAt) {
        this.quantity += quantity;
        this.lastMovedAt = movedAt;
    }

    public void subtractQuantity(Integer quantity, LocalDateTime movedAt) {
        this.quantity -= quantity;
        this.lastMovedAt = movedAt;
    }
}
