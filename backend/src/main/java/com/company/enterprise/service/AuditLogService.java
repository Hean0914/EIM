package com.company.enterprise.service;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.dto.AuditLogQuery;
import com.company.enterprise.entity.AuditLog;
import com.company.enterprise.vo.AuditLogSummaryVO;
import com.company.enterprise.vo.AuditLogVO;

public interface AuditLogService {

    PageResult<AuditLogVO> page(AuditLogQuery query);

    AuditLogSummaryVO summary(AuditLogQuery query);

    void record(AuditLog auditLog);
}
