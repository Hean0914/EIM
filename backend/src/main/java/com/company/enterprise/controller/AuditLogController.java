package com.company.enterprise.controller;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.Result;
import com.company.enterprise.common.RoleCodes;
import com.company.enterprise.common.annotation.RequireRoles;
import com.company.enterprise.dto.AuditLogQuery;
import com.company.enterprise.service.AuditLogService;
import com.company.enterprise.vo.AuditLogSummaryVO;
import com.company.enterprise.vo.AuditLogVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/audit-logs")
@RequireRoles({RoleCodes.GENERAL_MANAGER, RoleCodes.HR_MANAGER})
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/summary")
    public Result<AuditLogSummaryVO> summary(@Valid AuditLogQuery query) {
        return Result.success(auditLogService.summary(query));
    }

    @GetMapping("/page")
    public Result<PageResult<AuditLogVO>> page(@Valid AuditLogQuery query) {
        return Result.success(auditLogService.page(query));
    }
}
