package com.parking.controller;

import com.parking.dto.*;
import com.parking.entity.*;
import com.parking.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parking")
@CrossOrigin(origins = "http://localhost:3003")
public class ParkingController {

    @Autowired
    private ParkingService parkingService;

    @PostMapping("/entry")
    public Response<ParkingRecord> vehicleEntry(@Valid @RequestBody EntryRequest request) {
        return parkingService.vehicleEntry(request);
    }

    @GetMapping("/fee/{licensePlate}")
    public Response<Double> calculateFee(@PathVariable String licensePlate) {
        return parkingService.calculateFee(licensePlate);
    }

    @PostMapping("/pay")
    public Response<ParkingRecord> payFee(@Valid @RequestBody PaymentRequest request) {
        return parkingService.payFee(request);
    }

    @PostMapping("/exit")
    public Response<ParkingRecord> vehicleExit(@Valid @RequestBody ExitRequest request) {
        return parkingService.vehicleExit(request);
    }

    @GetMapping("/spots")
    public Response<List<ParkingSpot>> getAllSpots() {
        return parkingService.getAllSpots();
    }

    @PutMapping("/spots/adjust")
    public Response<ParkingSpot> adjustSpot(@Valid @RequestBody SpotAdjustRequest request) {
        return parkingService.adjustSpot(request);
    }

    @GetMapping("/records")
    public Response<List<ParkingRecord>> getAllRecords() {
        return parkingService.getAllRecords();
    }

    @GetMapping("/monthly")
    public Response<List<MonthlyVehicle>> getAllMonthlyVehicles() {
        return parkingService.getAllMonthlyVehicles();
    }

    @PostMapping("/monthly")
    public Response<MonthlyVehicle> addMonthlyVehicle(@RequestBody MonthlyVehicle vehicle) {
        return parkingService.addMonthlyVehicle(vehicle);
    }

    @GetMapping("/blacklist")
    public Response<List<BlacklistVehicle>> getBlacklist() {
        return parkingService.getBlacklist();
    }

    @PostMapping("/blacklist")
    public Response<BlacklistVehicle> addToBlacklist(@RequestBody BlacklistVehicle vehicle) {
        return parkingService.addToBlacklist(vehicle);
    }

    @DeleteMapping("/blacklist/{licensePlate}")
    public Response<Void> removeFromBlacklist(@PathVariable String licensePlate) {
        return parkingService.removeFromBlacklist(licensePlate);
    }

    @GetMapping("/alarms")
    public Response<List<AlarmRecord>> getAlarms() {
        return parkingService.getAlarms();
    }

    @GetMapping("/overview")
    public Response<Map<String, Object>> getOverview() {
        return parkingService.getOverview();
    }

    @GetMapping("/monthly/expiring")
    public Response<List<MonthlyVehicle>> getExpiringVehicles() {
        return parkingService.getExpiringVehicles();
    }

    @GetMapping("/statistics")
    public Response<List<DailyStatistics>> getDailyStatistics() {
        return parkingService.getDailyStatistics();
    }
}
