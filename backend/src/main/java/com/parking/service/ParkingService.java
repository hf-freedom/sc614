package com.parking.service;

import com.parking.dto.*;
import com.parking.entity.*;
import com.parking.repository.InMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ParkingService {

    @Autowired
    private InMemoryRepository repository;

    @Value("${parking.rate.temporary:10}")
    private double temporaryRate;

    @Value("${parking.grace-minutes:15}")
    private int graceMinutes;

    @Value("${parking.overtime-rate:5}")
    private double overtimeRate;

    public Response<ParkingRecord> vehicleEntry(EntryRequest request) {
        String licensePlate = request.getLicensePlate();

        if (repository.isBlacklisted(licensePlate)) {
            AlarmRecord alarm = new AlarmRecord();
            alarm.setAlarmId(UUID.randomUUID().toString());
            alarm.setLicensePlate(licensePlate);
            alarm.setAlarmType("BLACKLIST");
            alarm.setDescription("黑名单车辆试图入场");
            alarm.setAlarmTime(LocalDateTime.now());
            alarm.setHandled(false);
            repository.saveAlarm(alarm);
            return Response.error(403, "车辆在黑名单中，禁止入场");
        }

        ParkingRecord activeRecord = repository.getActiveRecord(licensePlate);
        if (activeRecord != null) {
            return Response.error(400, "车辆已在场内");
        }

        VehicleType vehicleType = VehicleType.TEMPORARY;
        MonthlyVehicle monthlyVehicle = repository.getMonthlyVehicle(licensePlate);
        if (monthlyVehicle != null && monthlyVehicle.isActive()) {
            if (monthlyVehicle.isExpired()) {
                return Response.error(400, "月租车已过期，请续费后入场");
            }
            vehicleType = VehicleType.MONTHLY;
        } else if ("MONTHLY".equalsIgnoreCase(request.getVehicleType())) {
            return Response.error(400, "未找到该车辆的月租信息");
        }

        List<ParkingSpot> availableSpots = repository.getAvailableSpots();
        if (availableSpots.isEmpty()) {
            return Response.error(503, "暂无空闲车位");
        }

        ParkingSpot assignedSpot = availableSpots.get(0);
        assignedSpot.setOccupied(true);
        assignedSpot.setLicensePlate(licensePlate);
        assignedSpot.setOccupiedTime(LocalDateTime.now());
        repository.saveSpot(assignedSpot);

        ParkingRecord record = new ParkingRecord();
        record.setRecordId(UUID.randomUUID().toString());
        record.setLicensePlate(licensePlate);
        record.setVehicleType(vehicleType);
        record.setEntryTime(LocalDateTime.now());
        record.setSpotId(assignedSpot.getSpotId());
        repository.saveRecord(record);

        DailyStatistics stats = repository.getStatisticsByDate(LocalDate.now());
        stats.setTotalEntries(stats.getTotalEntries() + 1);
        updateEmptySpots(stats);
        repository.saveStatistics(stats);

        return Response.success(record);
    }

    public Response<Double> calculateFee(String licensePlate) {
        ParkingRecord record = repository.getActiveRecord(licensePlate);
        if (record == null) {
            return Response.error(404, "未找到停车记录");
        }

        if (record.getVehicleType() == VehicleType.MONTHLY) {
            return Response.success(0.0);
        }

        double fee = calculateParkingFee(record.getEntryTime(), LocalDateTime.now());
        return Response.success(fee);
    }

    private double calculateParkingFee(LocalDateTime entryTime, LocalDateTime currentTime) {
        long hours = ChronoUnit.HOURS.between(entryTime, currentTime);
        if (ChronoUnit.MINUTES.between(entryTime, currentTime) % 60 > 0) {
            hours++;
        }
        return hours * temporaryRate;
    }

    public Response<ParkingRecord> payFee(PaymentRequest request) {
        ParkingRecord record = repository.getActiveRecord(request.getLicensePlate());
        if (record == null) {
            return Response.error(404, "未找到停车记录");
        }

        if (record.isPaid()) {
            return Response.error(400, "已支付，请勿重复支付");
        }

        double calculatedFee = calculateParkingFee(record.getEntryTime(), LocalDateTime.now());
        if (request.getAmount() < calculatedFee) {
            return Response.error(400, "支付金额不足，应支付: " + calculatedFee + " 元");
        }

        record.setPaid(true);
        record.setPaidTime(LocalDateTime.now());
        record.setTotalFee(calculatedFee);
        repository.saveRecord(record);

        DailyStatistics stats = repository.getStatisticsByDate(LocalDate.now());
        stats.setTotalRevenue(stats.getTotalRevenue() + calculatedFee);
        repository.saveStatistics(stats);

        return Response.success(record);
    }

    public Response<ParkingRecord> vehicleExit(ExitRequest request) {
        ParkingRecord record = repository.getActiveRecord(request.getLicensePlate());
        if (record == null) {
            return Response.error(404, "未找到停车记录");
        }

        if (record.getVehicleType() == VehicleType.TEMPORARY && !record.isPaid()) {
            return Response.error(400, "请先支付停车费");
        }

        LocalDateTime now = LocalDateTime.now();
        if (record.isPaid()) {
            long minutesSincePayment = ChronoUnit.MINUTES.between(record.getPaidTime(), now);
            if (minutesSincePayment > graceMinutes) {
                long overtimeHours = (minutesSincePayment - graceMinutes + 59) / 60;
                double overtimeFee = overtimeHours * overtimeRate;
                if (!request.isForceExit()) {
                    return Response.error(400, "支付后超时未出场，需追加超时费用: " + overtimeFee + " 元，请强制出场确认支付");
                }
                record.setTotalFee(record.getTotalFee() + overtimeFee);
                DailyStatistics stats = repository.getStatisticsByDate(LocalDate.now());
                stats.setTotalRevenue(stats.getTotalRevenue() + overtimeFee);
                repository.saveStatistics(stats);
            }
        }

        record.setExitTime(now);
        record.setExited(true);
        repository.saveRecord(record);

        ParkingSpot spot = repository.getSpot(record.getSpotId());
        if (spot != null) {
            spot.setOccupied(false);
            spot.setLicensePlate(null);
            spot.setOccupiedTime(null);
            repository.saveSpot(spot);
        }

        DailyStatistics stats = repository.getStatisticsByDate(LocalDate.now());
        stats.setTotalExits(stats.getTotalExits() + 1);
        if (request.isForceExit()) {
            record.setAbnormalExit(true);
            stats.setAbnormalExits(stats.getAbnormalExits() + 1);
        }
        updateEmptySpots(stats);
        repository.saveStatistics(stats);

        return Response.success(record);
    }

    public Response<List<ParkingSpot>> getAllSpots() {
        return Response.success(repository.getAllSpots());
    }

    public Response<ParkingSpot> adjustSpot(SpotAdjustRequest request) {
        ParkingSpot spot = repository.getSpot(request.getSpotId());
        if (spot == null) {
            return Response.error(404, "车位不存在");
        }

        if (request.isOccupied() && (request.getLicensePlate() == null || request.getLicensePlate().trim().isEmpty())) {
            return Response.error(400, "设置为占用状态时，车牌号不能为空");
        }

        spot.setOccupied(request.isOccupied());
        spot.setLicensePlate(request.isOccupied() ? request.getLicensePlate() : null);
        spot.setAbnormal(true);
        spot.setAbnormalReason(request.getReason());
        spot.setLastOperator(request.getOperator() != null ? request.getOperator() : "系统管理员");
        spot.setLastAdjustTime(LocalDateTime.now());
        if (!request.isOccupied()) {
            spot.setOccupiedTime(null);
        } else {
            spot.setOccupiedTime(LocalDateTime.now());
        }
        repository.saveSpot(spot);

        DailyStatistics stats = repository.getStatisticsByDate(LocalDate.now());
        updateEmptySpots(stats);
        repository.saveStatistics(stats);

        return Response.success(spot);
    }

    public Response<List<ParkingRecord>> getAllRecords() {
        return Response.success(repository.getAllRecords());
    }

    public Response<List<MonthlyVehicle>> getAllMonthlyVehicles() {
        return Response.success(new ArrayList<>(repository.getMonthlyVehicles().values()));
    }

    public Response<MonthlyVehicle> addMonthlyVehicle(MonthlyVehicle vehicle) {
        vehicle.setActive(true);
        repository.saveMonthlyVehicle(vehicle);
        return Response.success(vehicle);
    }

    public Response<List<BlacklistVehicle>> getBlacklist() {
        return Response.success(repository.getAllBlacklist());
    }

    public Response<BlacklistVehicle> addToBlacklist(BlacklistVehicle vehicle) {
        vehicle.setCreateTime(LocalDateTime.now());
        vehicle.setBlocked(true);
        repository.addToBlacklist(vehicle);
        return Response.success(vehicle);
    }

    public Response<Void> removeFromBlacklist(String licensePlate) {
        repository.removeFromBlacklist(licensePlate);
        return Response.success();
    }

    public Response<List<AlarmRecord>> getAlarms() {
        return Response.success(repository.getAllAlarms());
    }

    public Response<Map<String, Object>> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        List<ParkingSpot> allSpots = repository.getAllSpots();

        long totalSpots = allSpots.size();
        long occupiedSpots = allSpots.stream().filter(ParkingSpot::isOccupied).count();
        long availableSpots = allSpots.stream().filter(s -> !s.isOccupied() && !s.isAbnormal()).count();
        double occupancyRate = totalSpots > 0 ? (double) occupiedSpots / totalSpots * 100 : 0;

        Map<String, Long> spotsByZone = new HashMap<>();
        for (ParkingSpot spot : allSpots) {
            if (!spot.isOccupied()) {
                spotsByZone.put(spot.getZone(), spotsByZone.getOrDefault(spot.getZone(), 0L) + 1);
            }
        }

        overview.put("totalSpots", totalSpots);
        overview.put("occupiedSpots", occupiedSpots);
        overview.put("availableSpots", availableSpots);
        overview.put("occupancyRate", String.format("%.2f", occupancyRate) + "%");
        overview.put("spotsByZone", spotsByZone);

        DailyStatistics todayStats = repository.getStatisticsByDate(LocalDate.now());
        overview.put("todayRevenue", todayStats.getTotalRevenue());
        overview.put("todayEntries", todayStats.getTotalEntries());
        overview.put("todayExits", todayStats.getTotalExits());

        return Response.success(overview);
    }

    public Response<List<MonthlyVehicle>> getExpiringVehicles() {
        return Response.success(repository.getExpiringMonthlyVehicles());
    }

    public Response<List<DailyStatistics>> getDailyStatistics() {
        return Response.success(new ArrayList<>(repository.getStatistics().values()));
    }

    private void updateEmptySpots(DailyStatistics stats) {
        long availableSpots = repository.getAllSpots().stream()
                .filter(s -> !s.isOccupied() && !s.isAbnormal())
                .count();
        stats.setEmptySpots((int) availableSpots);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void dailyStatisticsTask() {
        LocalDate today = LocalDate.now();
        DailyStatistics stats = repository.getStatisticsByDate(today);
        updateEmptySpots(stats);
        repository.saveStatistics(stats);
    }
}
