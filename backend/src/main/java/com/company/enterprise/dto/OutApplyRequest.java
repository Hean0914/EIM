package com.company.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OutApplyRequest {

    private Integer outType = 1;

    @NotBlank(message = "不能为空")
    private String destination;

    @NotNull(message = "不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "不能为空")
    private LocalDateTime endTime;

    @NotBlank(message = "不能为空")
    private String reason;
}
