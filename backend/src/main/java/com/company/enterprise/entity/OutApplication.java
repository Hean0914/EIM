package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("t_out_application")
@EqualsAndHashCode(callSuper = true)
public class OutApplication extends BaseEntity {
    private String applyNo;
    private Long employeeId;
    private Integer outType;
    private String destination;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private Integer status;
    private Long approverId;
    private LocalDateTime approveTime;
    private String approveComment;
}
