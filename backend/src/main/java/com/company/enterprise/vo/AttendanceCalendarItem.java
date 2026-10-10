package com.company.enterprise.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class AttendanceCalendarItem {

    private LocalDate date;
    private boolean workday;
    private Long employeeId;
    private String employeeNo;
    private String employeeName;
    private Long deptId;
    private String deptName;
    private String status;
    private String statusText;
    private List<String> anomalies = new ArrayList<>();
    private boolean approvedLeave;
    private boolean approvedOut;
    private Integer leaveType;
    private String outDestination;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Integer workMinutes;
    private Integer checkInStatus;
    private Integer checkOutStatus;
    private Integer checkInGpsStatus;
    private Integer checkOutGpsStatus;
    private BigDecimal checkInDistance;
    private BigDecimal checkOutDistance;
    private String remark;
}
