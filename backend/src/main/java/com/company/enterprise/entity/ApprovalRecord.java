package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("t_approval_record")
@EqualsAndHashCode(callSuper = true)
public class ApprovalRecord extends BaseEntity {
    private String businessType;
    private Long businessId;
    private Long approverId;
    private Integer approvalResult;
    private String approvalComment;
    private LocalDateTime approvalTime;
}
