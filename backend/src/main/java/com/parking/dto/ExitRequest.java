package com.parking.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class ExitRequest {
    @NotBlank(message = "车牌号不能为空")
    private String licensePlate;
    private boolean forceExit;
}
