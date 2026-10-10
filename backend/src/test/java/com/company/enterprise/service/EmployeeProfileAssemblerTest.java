package com.company.enterprise.service;

import com.company.enterprise.entity.AttendanceRecord;
import com.company.enterprise.entity.AttendanceStat;
import com.company.enterprise.entity.Department;
import com.company.enterprise.entity.Employee;
import com.company.enterprise.entity.MeetingBooking;
import com.company.enterprise.entity.OutApplication;
import com.company.enterprise.entity.Plan;
import com.company.enterprise.entity.Position;
import com.company.enterprise.entity.Role;
import com.company.enterprise.entity.User;
import com.company.enterprise.vo.EmployeeProfileVO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EmployeeProfileAssemblerTest {

    private final EmployeeProfileAssembler assembler = new EmployeeProfileAssembler();

    @Test
    void assemblesMaskedIdentityAndMonthlyMetrics() {
        Employee employee = employee();
        Department department = new Department();
        department.setDeptName("技术研发部");
        Position position = new Position();
        position.setPositionName("研发工程师");
        User user = new User();
        user.setUsername("alice");
        user.setStatus(1);
        Role role = new Role();
        role.setRoleCode("RND_ENGINEER");
        role.setRoleName("研发工程师");

        AttendanceStat stat = new AttendanceStat();
        stat.setShouldWorkDays(new BigDecimal(20));
        stat.setActualWorkDays(new BigDecimal(19));
        stat.setLateCount(2);
        stat.setEarlyLeaveCount(1);
        stat.setAbsenceCount(1);
        stat.setLeaveDays(new BigDecimal(1.5));

        EmployeeProfileVO profile = assembler.assemble(
                employee, department, position, user, role, stat,
                List.of(attendanceRecord()), List.of(), List.of(outApplication()),
                List.of(plan()), List.of(meeting()), LocalDate.of(2026, 9, 15)
        );

        assertEquals("张三", profile.getEmployee().getEmployeeName());
        assertEquals("110***********1234", profile.getEmployee().getIdCard());
        assertEquals("138****5678", profile.getEmployee().getEmergencyPhone());
        assertEquals(95, profile.getMetrics().getAttendanceRate());
        assertEquals(2, profile.getMetrics().getLateCount());
        assertEquals(1, profile.getMetrics().getOutCount());
        assertEquals(1, profile.getMetrics().getActivePlanCount());
        assertEquals(1, profile.getMetrics().getUpcomingMeetingCount());
        assertFalse(profile.getAccount().isPasswordExposed());
    }

    @Test
    void sortsRecentWorkEventsByTimeDescending() {
        EmployeeProfileVO profile = assembler.assemble(
                employee(), null, null, null, null, null,
                List.of(attendanceRecord()), List.of(), List.of(outApplication()),
                List.of(plan()), List.of(meeting()), LocalDate.of(2026, 9, 15)
        );

        assertFalse(profile.getTimeline().isEmpty());
        for (int index = 1; index < profile.getTimeline().size(); index++) {
            assertFalse(profile.getTimeline().get(index - 1).getTime()
                    .isBefore(profile.getTimeline().get(index).getTime()));
        }
    }

    private Employee employee() {
        Employee employee = new Employee();
        employee.setId(7L);
        employee.setUserId(70L);
        employee.setEmployeeNo("E0007");
        employee.setEmployeeName("张三");
        employee.setDeptId(3L);
        employee.setPositionId(8L);
        employee.setIdCard("110101199001011234");
        employee.setPhone("13812345678");
        employee.setEmail("zhangsan@example.com");
        employee.setHireDate(LocalDate.of(2024, 1, 10));
        employee.setRegularDate(LocalDate.of(2024, 4, 10));
        employee.setEmploymentStatus(1);
        employee.setEmergencyContact("李四");
        employee.setEmergencyPhone("13887655678");
        return employee;
    }

    private AttendanceRecord attendanceRecord() {
        AttendanceRecord record = new AttendanceRecord();
        record.setId(101L);
        record.setAttendanceDate(LocalDate.of(2026, 9, 14));
        record.setCheckInTime(LocalDateTime.of(2026, 9, 14, 9, 10));
        record.setCheckOutTime(LocalDateTime.of(2026, 9, 14, 18, 5));
        record.setCheckInStatus(2);
        record.setCheckOutStatus(1);
        record.setWorkMinutes(535);
        record.setRemark("地铁延误");
        return record;
    }

    private OutApplication outApplication() {
        OutApplication out = new OutApplication();
        out.setId(201L);
        out.setStartTime(LocalDateTime.of(2026, 9, 12, 10, 0));
        out.setEndTime(LocalDateTime.of(2026, 9, 12, 12, 0));
        out.setStatus(1);
        out.setDestination("客户现场");
        out.setCreateTime(LocalDateTime.of(2026, 9, 11, 9, 0));
        return out;
    }

    private Plan plan() {
        Plan plan = new Plan();
        plan.setId(301L);
        plan.setTitle("研发迭代");
        plan.setStartDate(LocalDate.of(2026, 9, 1));
        plan.setEndDate(LocalDate.of(2026, 9, 30));
        plan.setProgress(60);
        plan.setStatus(1);
        plan.setPriority(3);
        plan.setCreateTime(LocalDateTime.of(2026, 9, 1, 9, 0));
        return plan;
    }

    private MeetingBooking meeting() {
        MeetingBooking meeting = new MeetingBooking();
        meeting.setId(401L);
        meeting.setMeetingTitle("需求评审");
        meeting.setStartTime(LocalDateTime.of(2026, 9, 16, 14, 0));
        meeting.setEndTime(LocalDateTime.of(2026, 9, 16, 15, 0));
        meeting.setStatus(0);
        meeting.setRoomName("3A会议室");
        meeting.setCreateTime(LocalDateTime.of(2026, 9, 10, 9, 0));
        return meeting;
    }
}
