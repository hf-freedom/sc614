package com.parking.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ParkingSpot {
    private String spotId;
    private String zone;
    private boolean occupied;
    private String licensePlate;
    private LocalDateTime occupiedTime;
    private boolean abnormal;
    private String abnormalReason;
    private String lastOperator;
    private LocalDateTime lastAdjustTime;

    public ParkingSpot(String spotId, String zone) {
        this.spotId = spotId;
        this.zone = zone;
        this.occupied = false;
        this.abnormal = false;
    }
}
