package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("t_plan_progress")
@EqualsAndHashCode(callSuper = true)
public class PlanProgress extends BaseEntity {
    private Long planId;
    private Integer progress;
    private String content;
    private Long submitterId;
}
