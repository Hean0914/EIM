package com.company.enterprise.vo;

import com.company.enterprise.entity.AuditLog;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuditLogVO extends AuditLog {
    private String statusLabel;
}
