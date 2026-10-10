package com.company.enterprise.service;

import com.company.enterprise.entity.AttendanceRecord;
import com.company.enterprise.entity.Employee;
import com.company.enterprise.entity.LeaveApplication;
import com.company.enterprise.vo.AttendanceAnalysisResult;
import com.company.enterprise.vo.AttendanceCalendarItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class AttendanceAnalysisCalculatorTest {

    @Test
    void calculatesCalendarStatusesAndSummaryFromExistingAttendanceData() {
        Employee employee = employee(1L);
        AttendanceRecord lateAndEarly = record(employee.getId(), LocalDate.of(2026, 9, 14));
        lateAndEarly.setCheckInTime(LocalDateTime.of(2026, 9, 14, 9, 12));
        lateAndEarly.setCheckOutTime(LocalDateTime.of(2026, 9, 14, 17, 40));
        lateAndEarly.setCheckInStatus(2);
        lateAndEarly.setCheckOutStatus(2);
        lateAndEarly.setWorkMinutes(508);

        LeaveApplication approvedLeave = new LeaveApplication();
        approvedLeave.setEmployeeId(employee.getId());
        approvedLeave.setStartTime(LocalDateTime.of(2026, 9, 15, 9, 0));
        approvedLeave.setEndTime(LocalDateTime.of(2026, 9, 15, 18, 0));
        approvedLeave.setStatus(1);

        AttendanceAnalysisResult result = AttendanceAnalysisCalculator.calculate(
                LocalDate.of(2026, 9, 14),
                LocalDate.of(2026, 9, 16),
                List.of(employee),
                List.of(lateAndEarly),
                List.of(approvedLeave),
                List.of(),
                LocalTime.of(9, 0),
                LocalTime.of(18, 0)
        );

        assertEquals(3, result.getCalendar().size());
        assertEquals(2, result.getShouldWorkDays());
        assertEquals(1, result.getActualWorkDays());
        assertEquals(50, result.getAttendanceRate());
        assertEquals(1, result.getLateCount());
        assertEquals(1, result.getEarlyLeaveCount());
        assertEquals(1, result.getAbsenceCount());

        AttendanceCalendarItem abnormal = result.getCalendar().get(0);
        assertEquals("ABNORMAL", abnormal.getStatus());
        assertIterableEquals(List.of("LATE", "EARLY_LEAVE"), abnormal.getAnomalies());
        assertEquals("LEAVE", result.getCalendar().get(1).getStatus());
        assertEquals("ABSENT", result.getCalendar().get(2).getStatus());
    }

    private Employee employee(Long id) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setEmployeeNo("E20260001");
        employee.setEmployeeName("Test Employee");
        employee.setDeptId(1L);
        employee.setHireDate(LocalDate.of(2026, 1, 1));
        employee.setEmploymentStatus(1);
        return employee;
    }

    private AttendanceRecord record(Long employeeId, LocalDate date) {
        AttendanceRecord record = new AttendanceRecord();
        record.setEmployeeId(employeeId);
        record.setAttendanceDate(date);
        record.setCheckInStatus(0);
        record.setCheckOutStatus(0);
        return record;
    }
}
