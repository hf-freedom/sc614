package com.parking.entity;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MonthlyVehicle {
    private String licensePlate;
    private String ownerName;
    private String phone;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;

    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }

    public boolean isExpiringSoon() {
        LocalDate now = LocalDate.now();
        return !isExpired() && now.plusDays(7).isAfter(endDate);
    }
}
