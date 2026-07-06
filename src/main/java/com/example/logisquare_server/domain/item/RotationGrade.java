package com.example.logisquare_server.domain.item;

import lombok.Getter;

@Getter
public enum RotationGrade {
    A("FAST"),
    B("NORMAL"),
    C("SLOW");

    private final String speed;

    RotationGrade(String speed) {
        this.speed = speed;
    }
}
