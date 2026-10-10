package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

@Data
@TableName("t_attendance_rule")
@EqualsAndHashCode(callSuper = true)
public class AttendanceRule extends BaseEntity {

    private String ruleName;
    private LocalTime workStartTime;
    private LocalTime workEndTime;
    private Integer status;
    private String remark;
}
