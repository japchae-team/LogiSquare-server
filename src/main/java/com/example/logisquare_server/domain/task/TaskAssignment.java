package com.example.logisquare_server.domain.task;

import com.example.logisquare_server.domain.common.BaseTimeEntity;
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
        name = "task_assignments",
        indexes = {
                @Index(name = "idx_task_assignments_task_id", columnList = "task_id"),
                @Index(name = "idx_task_assignments_worker_id", columnList = "worker_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskAssignment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private WorkTask task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "distance_score")
    private Integer distanceScore;

    @Column(name = "called_at")
    private LocalDateTime calledAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public TaskAssignment(
            WorkTask task,
            Worker worker,
            String status,
            Integer distanceScore,
            LocalDateTime calledAt,
            LocalDateTime respondedAt
    ) {
        this.task = task;
        this.worker = worker;
        this.status = status;
        this.distanceScore = distanceScore;
        this.calledAt = calledAt;
        this.respondedAt = respondedAt;
    }

    public void accept(LocalDateTime respondedAt) {
        this.status = "ACCEPTED";
        this.respondedAt = respondedAt;
    }

    public void reject(LocalDateTime respondedAt) {
        this.status = "REJECTED";
        this.respondedAt = respondedAt;
    }

    public void complete(LocalDateTime respondedAt) {
        this.status = "COMPLETED";
        this.respondedAt = respondedAt;
    }
}
