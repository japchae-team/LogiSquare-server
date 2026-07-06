package com.example.logisquare_server.domain.item;

import com.example.logisquare_server.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "rotation_grade", length = 10)
    private RotationGrade rotationGrade;

    @Column(name = "default_unit_qty", nullable = false)
    private Integer defaultUnitQty;

    public Item(String sku, String name, String category, RotationGrade rotationGrade, Integer defaultUnitQty) {
        this.sku = sku;
        this.name = name;
        this.category = category;
        this.rotationGrade = rotationGrade;
        this.defaultUnitQty = defaultUnitQty;
    }
}
