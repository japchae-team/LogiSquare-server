package com.example.logisquare_server.domain.task;

import com.example.logisquare_server.domain.common.BaseTimeEntity;
import com.example.logisquare_server.domain.item.Item;
import com.example.logisquare_server.domain.location.StorageLocation;
import com.example.logisquare_server.domain.worker.Worker;
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
        name = "work_tasks",
        indexes = {
                @Index(name = "idx_work_tasks_item_id", columnList = "item_id"),
                @Index(name = "idx_work_tasks_source_location_id", columnList = "source_location_id"),
                @Index(name = "idx_work_tasks_target_location_id", columnList = "target_location_id"),
                @Index(name = "idx_work_tasks_requested_by", columnList = "requested_by")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkTask extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_type", nullable = false, length = 50)
    private String taskType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_location_id")
    private StorageLocation sourceLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_location_id")
    private StorageLocation targetLocation;

    private Integer quantity;

    @Column(nullable = false, length = 30)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by")
    private Worker requestedBy;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public WorkTask(
            String taskType,
            Item item,
            StorageLocation sourceLocation,
            StorageLocation targetLocation,
            Integer quantity,
            String status,
            Worker requestedBy,
            LocalDateTime acceptedAt,
            LocalDateTime completedAt
    ) {
        this.taskType = taskType;
        this.item = item;
        this.sourceLocation = sourceLocation;
        this.targetLocation = targetLocation;
        this.quantity = quantity;
        this.status = status;
        this.requestedBy = requestedBy;
        this.acceptedAt = acceptedAt;
        this.completedAt = completedAt;
    }
}
