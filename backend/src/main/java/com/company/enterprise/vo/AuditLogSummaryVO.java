package com.company.enterprise.vo;

import lombok.Data;

@Data
public class AuditLogSummaryVO {
    private Long total = 0L;
    private Long success = 0L;
    private Long failure = 0L;
}
