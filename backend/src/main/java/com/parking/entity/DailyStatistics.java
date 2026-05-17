package com.parking.entity;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DailyStatistics {
    private LocalDate date;
    private Double totalRevenue;
    private Integer emptySpots;
    private Integer abnormalExits;
    private Integer totalEntries;
    private Integer totalExits;
}
