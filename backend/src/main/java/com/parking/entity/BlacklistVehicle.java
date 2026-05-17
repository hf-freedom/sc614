package com.parking.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BlacklistVehicle {
    private String licensePlate;
    private String reason;
    private LocalDateTime createTime;
    private boolean blocked;
}
