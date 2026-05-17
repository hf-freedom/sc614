package com.parking.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class SpotAdjustRequest {
    @NotBlank(message = "车位ID不能为空")
    private String spotId;
    private boolean occupied;
    private String licensePlate;
    @NotBlank(message = "调整原因不能为空")
    private String reason;
    private String operator;
}
