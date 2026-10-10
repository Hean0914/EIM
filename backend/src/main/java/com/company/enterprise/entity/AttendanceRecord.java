package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@TableName("t_attendance_record")
@EqualsAndHashCode(callSuper = true)
public class AttendanceRecord extends BaseEntity {
    private Long employeeId;
    private LocalDate attendanceDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Integer checkInStatus;
    private Integer checkOutStatus;
    private Integer workMinutes;
    private String checkInLocation;
    private String checkOutLocation;
    private BigDecimal checkInLongitude;
    private BigDecimal checkInLatitude;
    private BigDecimal checkInAccuracy;
    private BigDecimal checkInDistance;
    private Integer checkInGpsStatus;
    private BigDecimal checkOutLongitude;
    private BigDecimal checkOutLatitude;
    private BigDecimal checkOutAccuracy;
    private BigDecimal checkOutDistance;
    private Integer checkOutGpsStatus;
    private String remark;

    @TableField(exist = false)
    private String employeeNo;

    @TableField(exist = false)
    private String employeeName;

    @TableField(exist = false)
    private Long deptId;

    @TableField(exist = false)
    private String deptName;
}
