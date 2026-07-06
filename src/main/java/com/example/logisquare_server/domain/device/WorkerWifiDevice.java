package com.example.logisquare_server.domain.device;

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
        name = "worker_wifi_devices",
        indexes = {
                @Index(name = "idx_worker_wifi_devices_worker_id", columnList = "worker_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkerWifiDevice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;

    @Column(name = "device_identifier", nullable = false, unique = true, length = 100)
    private String deviceIdentifier;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    public WorkerWifiDevice(Worker worker, String deviceIdentifier, String deviceType, String status, LocalDateTime issuedAt) {
        this.worker = worker;
        this.deviceIdentifier = deviceIdentifier;
        this.deviceType = deviceType;
        this.status = status;
        this.issuedAt = issuedAt;
    }
}
