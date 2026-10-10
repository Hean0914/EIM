package com.company.enterprise.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AttendanceCalendarQuery {

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate = LocalDate.now().withDayOfMonth(1);

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

    private Long employeeId;
}
