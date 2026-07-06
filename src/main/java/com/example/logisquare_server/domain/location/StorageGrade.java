package com.example.logisquare_server.domain.location;

import lombok.Getter;

@Getter
public enum StorageGrade {
    A("FAST"),
    B("NORMAL"),
    C("SLOW");

    private final String speed;

    StorageGrade(String speed) {
        this.speed = speed;
    }
}
