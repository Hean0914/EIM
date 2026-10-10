package com.company.enterprise.service.impl;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.dto.AuditLogQuery;
import com.company.enterprise.entity.AuditLog;
import com.company.enterprise.mapper.AuditLogMapper;
import com.company.enterprise.service.AuditLogAssembler;
import com.company.enterprise.service.AuditLogService;
import com.company.enterprise.vo.AuditLogSummaryVO;
import com.company.enterprise.vo.AuditLogVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogMapper auditLogMapper;

    public AuditLogServiceImpl(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    public PageResult<AuditLogVO> page(AuditLogQuery query) {
        long current = ServiceSupport.normalizeCurrent(query.getCurrent());
        long size = ServiceSupport.normalizeSize(query.getSize());
        long offset = (current - 1) * size;
        Long total = auditLogMapper.countPage(query);
        List<AuditLog> records = total == null || total == 0
                ? List.of()
                : auditLogMapper.selectPage(query, offset, size);
        List<AuditLogVO> views = records.stream().map(this::toView).toList();
        return new PageResult<>(total == null ? 0 : total, current, size, views);
    }

    @Override
    public AuditLogSummaryVO summary(AuditLogQuery query) {
        AuditLogSummaryVO summary = new AuditLogSummaryVO();
        summary.setTotal(valueOf(auditLogMapper.countPage(query)));
        summary.setSuccess(valueOf(auditLogMapper.countByStatus(query, 1)));
        summary.setFailure(valueOf(auditLogMapper.countByStatus(query, 0)));
        return summary;
    }

    @Override
    public void record(AuditLog auditLog) {
        if (auditLog == null || auditLog.getUserId() == null) {
            return;
        }
        auditLog.setDetail(AuditLogAssembler.sanitizeDetail(auditLog.getDetail()));
        auditLogMapper.insertAuditLog(auditLog);
    }

    private AuditLogVO toView(AuditLog log) {
        AuditLogVO view = new AuditLogVO();
        view.setId(log.getId());
        view.setUserId(log.getUserId());
        view.setUsername(log.getUsername());
        view.setRealName(log.getRealName());
        view.setRoleCode(log.getRoleCode());
        view.setModule(log.getModule());
        view.setAction(log.getAction());
        view.setTargetType(log.getTargetType());
        view.setTargetId(log.getTargetId());
        view.setRequestMethod(log.getRequestMethod());
        view.setRequestPath(log.getRequestPath());
        view.setOperationStatus(log.getOperationStatus());
        view.setDetail(AuditLogAssembler.sanitizeDetail(log.getDetail()));
        view.setClientIp(log.getClientIp());
        view.setUserAgent(log.getUserAgent());
        view.setCreateTime(log.getCreateTime());
        view.setUpdateTime(log.getUpdateTime());
        view.setIsDeleted(log.getIsDeleted());
        view.setStatusLabel(AuditLogAssembler.statusLabel(log.getOperationStatus()));
        return view;
    }

    private long valueOf(Long value) {
        return value == null ? 0L : value;
    }
}
