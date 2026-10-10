package com.company.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LeaveApplyRequest {

    @NotNull(message = "不能为空")
    private Integer leaveType;

    @NotNull(message = "不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "不能为空")
    private LocalDateTime endTime;

    @NotNull(message = "不能为空")
    private BigDecimal leaveDays;

    @NotBlank(message = "不能为空")
    private String reason;
}
