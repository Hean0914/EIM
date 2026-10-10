package com.company.enterprise.controller;

import com.company.enterprise.common.annotation.RequireRoles;
import com.company.enterprise.common.RoleCodes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuditLogControllerAccessTest {

    @Test
    void auditLogControllerIsRestrictedToGeneralManagerAndHrManager() {
        RequireRoles annotation = AuditLogController.class.getAnnotation(RequireRoles.class);

        assertNotNull(annotation);
        assertArrayEquals(new String[]{RoleCodes.GENERAL_MANAGER, RoleCodes.HR_MANAGER}, annotation.value());
    }
}
