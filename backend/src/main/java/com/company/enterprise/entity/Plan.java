package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@TableName("t_plan")
@EqualsAndHashCode(callSuper = true)
public class Plan extends BaseEntity {
    private String planNo;
    private Integer planType;
    private String title;
    private String content;
    private Long deptId;
    private Long employeeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer priority;
    private Integer progress;
    private Integer status;
    private Long creatorId;
}
