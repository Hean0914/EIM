package com.company.enterprise.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AttendanceRecordQuery {

    @Min(value = 1, message = "必须大于等于1")
    private Long current = 1L;

    @Min(value = 1, message = "必须大于等于1")
    private Long size = 10L;

    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
}
