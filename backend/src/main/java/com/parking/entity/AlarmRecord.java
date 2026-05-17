package com.parking.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AlarmRecord {
    private String alarmId;
    private String licensePlate;
    private String alarmType;
    private String description;
    private LocalDateTime alarmTime;
    private boolean handled;
}
