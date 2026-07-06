package com.example.logisquare_server.domain.device;

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
@Table(name = "wifi_access_points")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WifiAccessPoint extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ap_code", nullable = false, unique = true, length = 50)
    private String apCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String ssid;

    @Column(nullable = false, unique = true, length = 50)
    private String bssid;

    @Column(name = "pos_x")
    private Integer posX;

    @Column(name = "pos_y")
    private Integer posY;

    @Column(nullable = false)
    private Boolean active = true;

    public WifiAccessPoint(String apCode, String name, String ssid, String bssid, Integer posX, Integer posY, Boolean active) {
        this.apCode = apCode;
        this.name = name;
        this.ssid = ssid;
        this.bssid = bssid;
        this.posX = posX;
        this.posY = posY;
        this.active = active != null ? active : true;
    }
}
