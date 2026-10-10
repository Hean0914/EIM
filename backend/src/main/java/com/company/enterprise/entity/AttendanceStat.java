package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("t_attendance_stat")
@EqualsAndHashCode(callSuper = true)
public class AttendanceStat extends BaseEntity {
    private Long employeeId;
    private String statMonth;
    private BigDecimal shouldWorkDays;
    private BigDecimal actualWorkDays;
    private Integer lateCount;
    private Integer earlyLeaveCount;
    private Integer absenceCount;
    private BigDecimal leaveDays;
    private BigDecimal outDays;
    private BigDecimal overtimeHours;
}
