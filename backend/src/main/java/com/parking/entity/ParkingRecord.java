package com.parking.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ParkingRecord {
    private String recordId;
    private String licensePlate;
    private VehicleType vehicleType;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private String spotId;
    private Double totalFee;
    private boolean paid;
    private LocalDateTime paidTime;
    private boolean exited;
    private boolean abnormalExit;

    public ParkingRecord() {
        this.paid = false;
        this.exited = false;
        this.abnormalExit = false;
    }
}
