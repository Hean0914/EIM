package com.company.enterprise.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogQuery {

    @Min(value = 1, message = "必须大于等于1")
    private Long current = 1L;

    @Min(value = 1, message = "必须大于等于1")
    private Long size = 20L;

    private String module;
    private String action;
    private Integer operationStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
