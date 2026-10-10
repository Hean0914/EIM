package com.company.enterprise.vo;

import lombok.Data;

@Data
public class AttendanceEmployeeRanking {

    private Long employeeId;
    private String employeeNo;
    private String employeeName;
    private String deptName;
    private int anomalyCount;
    private int lateCount;
    private int earlyLeaveCount;
    private int missingCheckInCount;
    private int missingCheckOutCount;
    private int absenceCount;
    private int shouldWorkDays;
    private int actualWorkDays;
    private int attendanceRate;
}
