package com.company.enterprise.service;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttendanceRuleTimeTest {

    @Test
    void checkInAtWorkStartIsNormalAndAfterWorkStartIsLate() {
        LocalTime workStart = LocalTime.of(9, 0);
        assertFalse(workStart.isAfter(workStart));
        assertTrue(LocalTime.of(9, 0, 1).isAfter(workStart));
    }

    @Test
    void checkOutAtWorkEndIsNormalAndBeforeWorkEndIsEarly() {
        LocalTime workEnd = LocalTime.of(18, 0);
        assertFalse(workEnd.isBefore(workEnd));
        assertTrue(LocalTime.of(17, 59, 59).isBefore(workEnd));
    }
}
