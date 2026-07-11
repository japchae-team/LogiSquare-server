package com.example.logisquare_server.domain.safety;

import com.example.logisquare_server.domain.common.BaseTimeEntity;
import com.example.logisquare_server.domain.device.CctvCamera;
import com.example.logisquare_server.domain.location.StorageLocation;
import com.example.logisquare_server.domain.user.User;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "safety_events",
        indexes = {
                @Index(name = "idx_safety_events_worker_id", columnList = "worker_id"),
                @Index(name = "idx_safety_events_storage_location_id", columnList = "storage_location_id"),
                @Index(name = "idx_safety_events_camera_id", columnList = "camera_id"),
                @Index(name = "idx_safety_events_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SafetyEvent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    private Worker worker;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "storage_location_id", nullable = false)
    private StorageLocation storageLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camera_id")
    private CctvCamera camera;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "source_type", nullable = false, length = 50)
    private String sourceType;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "capture_url")
    private String captureUrl;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "helmet_worn")
    private Boolean helmetWorn;

    @Column(name = "vest_worn")
    private Boolean vestWorn;

    @Column(name = "shoes_worn")
    private Boolean shoesWorn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "resolution_memo", length = 1000)
    private String resolutionMemo;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    public SafetyEvent(
            Worker worker,
            StorageLocation storageLocation,
            CctvCamera camera,
            String eventType,
            String sourceType,
            String status,
            String captureUrl,
            BigDecimal confidenceScore,
            Boolean helmetWorn,
            Boolean vestWorn,
            Boolean shoesWorn,
            User assignedBy,
            LocalDateTime assignedAt,
            User resolvedBy,
            String resolutionMemo,
            LocalDateTime occurredAt,
            LocalDateTime resolvedAt
    ) {
        this.worker = worker;
        this.storageLocation = storageLocation;
        this.camera = camera;
        this.eventType = eventType;
        this.sourceType = sourceType;
        this.status = status;
        this.captureUrl = captureUrl;
        this.confidenceScore = confidenceScore;
        this.helmetWorn = helmetWorn;
        this.vestWorn = vestWorn;
        this.shoesWorn = shoesWorn;
        this.assignedBy = assignedBy;
        this.assignedAt = assignedAt;
        this.resolvedBy = resolvedBy;
        this.resolutionMemo = resolutionMemo;
        this.occurredAt = occurredAt;
        this.resolvedAt = resolvedAt;
    }

    public void assignWorker(Worker worker, User assignedBy, LocalDateTime assignedAt) {
        this.worker = worker;
        this.assignedBy = assignedBy;
        this.assignedAt = assignedAt;
        if (!"RESOLVED".equals(this.status)) {
            this.status = "ASSIGNED";
        }
    }

    public void resolve(User resolvedBy, String resolutionMemo, LocalDateTime resolvedAt) {
        this.resolvedBy = resolvedBy;
        this.resolutionMemo = resolutionMemo;
        this.resolvedAt = resolvedAt;
        this.status = "RESOLVED";
    }
}
