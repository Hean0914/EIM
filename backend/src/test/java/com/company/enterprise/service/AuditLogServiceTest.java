package com.company.enterprise.service;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.dto.AuditLogQuery;
import com.company.enterprise.entity.AuditLog;
import com.company.enterprise.mapper.AuditLogMapper;
import com.company.enterprise.service.impl.AuditLogServiceImpl;
import com.company.enterprise.vo.AuditLogVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogMapper auditLogMapper;

    @Test
    void pageAddsStatusLabelAndRedactsSensitiveDetail() {
        AuditLogQuery query = new AuditLogQuery();
        AuditLog log = new AuditLog();
        log.setId(4L);
        log.setOperationStatus(1);
        log.setDetail("token=secret password=abc123 planId=8");
        log.setCreateTime(LocalDateTime.of(2026, 9, 16, 9, 0));
        when(auditLogMapper.countPage(query)).thenReturn(1L);
        when(auditLogMapper.selectPage(query, 0L, 20L)).thenReturn(List.of(log));

        PageResult<AuditLogVO> result = new AuditLogServiceImpl(auditLogMapper).page(query);

        assertEquals("成功", result.getRecords().get(0).getStatusLabel());
        assertEquals("token=[REDACTED] password=[REDACTED] planId=8", result.getRecords().get(0).getDetail());
    }
}
