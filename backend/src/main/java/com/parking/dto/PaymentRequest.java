package com.parking.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class PaymentRequest {
    @NotBlank(message = "车牌号不能为空")
    private String licensePlate;
    @NotNull(message = "支付金额不能为空")
    private Double amount;
}
