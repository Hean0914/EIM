package com.company.enterprise.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class EmployeeProfileVO {

    private EmployeeInfo employee = new EmployeeInfo();
    private Metrics metrics = new Metrics();
    private AccountInfo account = new AccountInfo();
    private List<AttendanceTrendItem> attendanceTrend = new ArrayList<>();
    private List<TimelineItem> timeline = new ArrayList<>();
    private List<PlanItem> plans = new ArrayList<>();
    private List<MeetingItem> meetings = new ArrayList<>();

    @Data
    public static class EmployeeInfo {
        private Long id;
        private Long userId;
        private String employeeNo;
        private String employeeName;
        private Integer gender;
        private Long deptId;
        private String deptName;
        private Long positionId;
        private String positionName;
        private String phone;
        private String email;
        private LocalDate hireDate;
        private LocalDate regularDate;
        private LocalDate resignDate;
        private Integer employmentStatus;
        private String employmentStatusLabel;
        private Integer serviceYears;
        private String idCard;
        private String address;
        private String emergencyContact;
        private String emergencyPhone;
        private String remark;
    }

    @Data
    public static class Metrics {
        private Integer attendanceRate = 0;
        private Integer actualWorkDays = 0;
        private Integer shouldWorkDays = 0;
        private Integer lateCount = 0;
        private Integer earlyLeaveCount = 0;
        private Integer absenceCount = 0;
        private BigDecimal leaveDays = BigDecimal.ZERO;
        private BigDecimal outDays = BigDecimal.ZERO;
        private Integer outCount = 0;
        private Integer activePlanCount = 0;
        private Integer upcomingMeetingCount = 0;
    }

    @Data
    public static class AccountInfo {
        private boolean accountAssigned;
        private String username;
        private Integer accountStatus;
        private String roleCode;
        private String roleName;

        @JsonIgnore
        private boolean passwordExposed;
    }

    @Data
    public static class AttendanceTrendItem {
        private LocalDate date;
        private String status;
        private String statusText;
        private LocalDateTime checkInTime;
        private LocalDateTime checkOutTime;
        private Integer workMinutes;
        private List<String> anomalies = new ArrayList<>();
    }

    @Data
    public static class TimelineItem {
        private String key;
        private String type;
        private String title;
        private String description;
        private LocalDateTime time;
        private String status;
    }

    @Data
    public static class PlanItem {
        private Long id;
        private String planNo;
        private String title;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer progress;
        private Integer priority;
        private Integer status;
    }

    @Data
    public static class MeetingItem {
        private Long id;
        private String meetingTitle;
        private String roomName;
        private Integer participantCount;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer status;
    }
}
