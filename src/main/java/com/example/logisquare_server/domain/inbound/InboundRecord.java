package com.example.logisquare_server.domain.inbound;

import com.example.logisquare_server.domain.common.BaseTimeEntity;
import com.example.logisquare_server.domain.item.Item;
import com.example.logisquare_server.domain.location.StorageLocation;
import com.example.logisquare_server.domain.user.User;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "inbound_records",
        indexes = {
                @Index(name = "idx_inbound_records_item_id", columnList = "item_id"),
                @Index(name = "idx_inbound_records_target_location_id", columnList = "target_location_id"),
                @Index(name = "idx_inbound_records_created_by", columnList = "created_by")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InboundRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_location_id", nullable = false)
    private StorageLocation targetLocation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    public InboundRecord(
            Item item,
            Integer quantity,
            StorageLocation targetLocation,
            User createdBy,
            String status,
            LocalDateTime receivedAt
    ) {
        this.item = item;
        this.quantity = quantity;
        this.targetLocation = targetLocation;
        this.createdBy = createdBy;
        this.status = status;
        this.receivedAt = receivedAt;
    }
}
