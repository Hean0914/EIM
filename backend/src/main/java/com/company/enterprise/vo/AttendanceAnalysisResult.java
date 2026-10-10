package com.company.enterprise.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class AttendanceAnalysisResult {

    private LocalDate startDate;
    private LocalDate endDate;
    private int employeeCount;
    private int shouldWorkDays;
    private int actualWorkDays;
    private int attendanceRate;
    private int lateCount;
    private int earlyLeaveCount;
    private int missingCheckInCount;
    private int missingCheckOutCount;
    private int absenceCount;
    private int gpsAbnormalCount;
    private List<AttendanceCalendarItem> calendar = new ArrayList<>();
    private List<AttendanceDailyTrend> dailyTrend = new ArrayList<>();
    private List<AttendanceAnomalyDistribution> anomalyDistribution = new ArrayList<>();
    private List<AttendanceEmployeeRanking> employeeRanking = new ArrayList<>();
}
