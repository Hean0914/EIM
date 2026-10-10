package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("t_audit_log")
@EqualsAndHashCode(callSuper = true)
public class AuditLog extends BaseEntity {
    private Long userId;
    private String username;
    private String realName;
    private String roleCode;
    private String module;
    private String action;
    private String targetType;
    private Long targetId;
    private String requestMethod;
    private String requestPath;
    private Integer operationStatus;
    private String detail;
    private String clientIp;
    private String userAgent;
}
