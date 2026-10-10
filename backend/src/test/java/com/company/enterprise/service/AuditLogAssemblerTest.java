package com.company.enterprise.service;

import com.company.enterprise.entity.AuditLog;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditLogAssemblerTest {

    @Test
    void formatsOperationStatusAndRedactsSensitiveText() {
        AuditLog success = new AuditLog();
        success.setOperationStatus(1);
        success.setDetail("password=Admin@123456 token=secret-token planId=12");

        assertEquals("成功", AuditLogAssembler.statusLabel(success.getOperationStatus()));
        assertEquals("password=[REDACTED] token=[REDACTED] planId=12", AuditLogAssembler.sanitizeDetail(success.getDetail()));
    }

    @Test
    void usesFailureLabelForFailedOperations() {
        assertEquals("失败", AuditLogAssembler.statusLabel(0));
        assertEquals("未知", AuditLogAssembler.statusLabel(9));
    }
}
