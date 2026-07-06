package com.example.logisquare_server.domain.location;

import com.example.logisquare_server.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "storage_locations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StorageLocation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "area_code", length = 50)
    private String areaCode;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "location_grade", length = 30)
    private String locationGrade;

    @Column(name = "location_type", length = 50)
    private String locationType;

    @Column(name = "danger_area", nullable = false)
    private Boolean dangerArea = false;

    @Column(name = "pos_x")
    private Integer posX;

    @Column(name = "pos_y")
    private Integer posY;

    private Integer capacity;

    @Column(nullable = false)
    private Boolean active = true;

    public StorageLocation(
            String areaCode,
            String code,
            String name,
            String locationGrade,
            String locationType,
            Boolean dangerArea,
            Integer posX,
            Integer posY,
            Integer capacity,
            Boolean active
    ) {
        this.areaCode = areaCode;
        this.code = code;
        this.name = name;
        this.locationGrade = locationGrade;
        this.locationType = locationType;
        this.dangerArea = dangerArea != null ? dangerArea : false;
        this.posX = posX;
        this.posY = posY;
        this.capacity = capacity;
        this.active = active != null ? active : true;
    }
}
