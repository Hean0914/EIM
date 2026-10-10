package com.company.enterprise.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RoleCodesTest {

    @Test
    void mapsConfiguredPositionCodesToRoleCodes() {
        assertEquals(RoleCodes.GENERAL_MANAGER, RoleCodes.forPositionCode("GENERAL_MANAGER"));
        assertEquals(RoleCodes.HR_MANAGER, RoleCodes.forPositionCode("HR_MANAGER"));
        assertEquals(RoleCodes.RND_ENGINEER, RoleCodes.forPositionCode("DEVELOPER"));
        assertEquals(RoleCodes.SALES_SPECIALIST, RoleCodes.forPositionCode("SALES"));
        assertEquals(RoleCodes.ACCOUNTANT, RoleCodes.forPositionCode("ACCOUNTANT"));
        assertNull(RoleCodes.forPositionCode("UNKNOWN"));
    }
}
