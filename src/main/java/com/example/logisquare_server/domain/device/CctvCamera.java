package com.example.logisquare_server.domain.device;

import com.example.logisquare_server.domain.common.BaseTimeEntity;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "cctv_cameras",
        indexes = {
                @Index(name = "idx_cctv_cameras_storage_location_id", columnList = "storage_location_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CctvCamera extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "storage_location_id", nullable = false)
    private StorageLocation storageLocation;

    @Column(name = "camera_code", nullable = false, unique = true, length = 50)
    private String cameraCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "stream_url")
    private String streamUrl;

    @Column(nullable = false)
    private Boolean active = true;

    public CctvCamera(StorageLocation storageLocation, String cameraCode, String name, String streamUrl, Boolean active) {
        this.storageLocation = storageLocation;
        this.cameraCode = cameraCode;
        this.name = name;
        this.streamUrl = streamUrl;
        this.active = active != null ? active : true;
    }
}
