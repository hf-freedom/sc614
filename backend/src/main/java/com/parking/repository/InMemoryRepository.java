package com.parking.repository;

import com.parking.entity.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class InMemoryRepository {

    private final Map<String, ParkingSpot> spots = new ConcurrentHashMap<>();
    private final Map<String, ParkingRecord> records = new ConcurrentHashMap<>();
    private final Map<String, MonthlyVehicle> monthlyVehicles = new ConcurrentHashMap<>();
    private final Map<String, BlacklistVehicle> blacklist = new ConcurrentHashMap<>();
    private final Map<String, AlarmRecord> alarms = new ConcurrentHashMap<>();
    private final Map<LocalDate, DailyStatistics> statistics = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        String[] zones = {"A", "B", "C", "D"};
        for (String zone : zones) {
            for (int i = 1; i <= 25; i++) {
                String spotId = zone + String.format("%02d", i);
                spots.put(spotId, new ParkingSpot(spotId, zone));
            }
        }

        MonthlyVehicle mv1 = new MonthlyVehicle();
        mv1.setLicensePlate("京A12345");
        mv1.setOwnerName("张三");
        mv1.setPhone("13800138000");
        mv1.setStartDate(LocalDate.now());
        mv1.setEndDate(LocalDate.now().plusMonths(1));
        mv1.setActive(true);
        monthlyVehicles.put(mv1.getLicensePlate(), mv1);

        MonthlyVehicle mv2 = new MonthlyVehicle();
        mv2.setLicensePlate("京B67890");
        mv2.setOwnerName("李四");
        mv2.setPhone("13900139000");
        mv2.setStartDate(LocalDate.now().minusDays(30));
        mv2.setEndDate(LocalDate.now().minusDays(1));
        mv2.setActive(true);
        monthlyVehicles.put(mv2.getLicensePlate(), mv2);
    }

    public Map<String, ParkingSpot> getSpots() {
        return spots;
    }

    public List<ParkingSpot> getAllSpots() {
        return new ArrayList<>(spots.values());
    }

    public List<ParkingSpot> getAvailableSpots() {
        return spots.values().stream()
                .filter(s -> !s.isOccupied() && !s.isAbnormal())
                .collect(Collectors.toList());
    }

    public ParkingSpot getSpot(String spotId) {
        return spots.get(spotId);
    }

    public void saveSpot(ParkingSpot spot) {
        spots.put(spot.getSpotId(), spot);
    }

    public Map<String, ParkingRecord> getRecords() {
        return records;
    }

    public List<ParkingRecord> getAllRecords() {
        return new ArrayList<>(records.values());
    }

    public ParkingRecord getActiveRecord(String licensePlate) {
        return records.values().stream()
                .filter(r -> r.getLicensePlate().equals(licensePlate) && !r.isExited())
                .findFirst()
                .orElse(null);
    }

    public void saveRecord(ParkingRecord record) {
        records.put(record.getRecordId(), record);
    }

    public Map<String, MonthlyVehicle> getMonthlyVehicles() {
        return monthlyVehicles;
    }

    public MonthlyVehicle getMonthlyVehicle(String licensePlate) {
        return monthlyVehicles.get(licensePlate);
    }

    public void saveMonthlyVehicle(MonthlyVehicle vehicle) {
        monthlyVehicles.put(vehicle.getLicensePlate(), vehicle);
    }

    public List<MonthlyVehicle> getExpiringMonthlyVehicles() {
        return monthlyVehicles.values().stream()
                .filter(MonthlyVehicle::isExpiringSoon)
                .collect(Collectors.toList());
    }

    public Map<String, BlacklistVehicle> getBlacklist() {
        return blacklist;
    }

    public boolean isBlacklisted(String licensePlate) {
        BlacklistVehicle vehicle = blacklist.get(licensePlate);
        return vehicle != null && vehicle.isBlocked();
    }

    public void addToBlacklist(BlacklistVehicle vehicle) {
        blacklist.put(vehicle.getLicensePlate(), vehicle);
    }

    public void removeFromBlacklist(String licensePlate) {
        blacklist.remove(licensePlate);
    }

    public List<BlacklistVehicle> getAllBlacklist() {
        return new ArrayList<>(blacklist.values());
    }

    public Map<String, AlarmRecord> getAlarms() {
        return alarms;
    }

    public void saveAlarm(AlarmRecord alarm) {
        alarms.put(alarm.getAlarmId(), alarm);
    }

    public List<AlarmRecord> getAllAlarms() {
        return new ArrayList<>(alarms.values());
    }

    public Map<LocalDate, DailyStatistics> getStatistics() {
        return statistics;
    }

    public DailyStatistics getStatisticsByDate(LocalDate date) {
        return statistics.computeIfAbsent(date, k -> {
            DailyStatistics ds = new DailyStatistics();
            ds.setDate(date);
            ds.setTotalRevenue(0.0);
            ds.setEmptySpots(0);
            ds.setAbnormalExits(0);
            ds.setTotalEntries(0);
            ds.setTotalExits(0);
            return ds;
        });
    }

    public void saveStatistics(DailyStatistics ds) {
        statistics.put(ds.getDate(), ds);
    }
}
