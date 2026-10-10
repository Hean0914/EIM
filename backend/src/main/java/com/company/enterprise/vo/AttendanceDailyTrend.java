package com.company.enterprise.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AttendanceDailyTrend {

    private LocalDate date;
    private boolean workday;
    private int normalCount;
    private int anomalyCount;
    private int leaveCount;
    private int outCount;
    private int absentCount;
}
