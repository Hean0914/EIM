package com.company.enterprise.service;

import com.company.enterprise.common.TokenUser;
import com.company.enterprise.common.UserContext;
import com.company.enterprise.mapper.DashboardMapper;
import com.company.enterprise.service.impl.DashboardServiceImpl;
import com.company.enterprise.vo.AdminDashboardVO;
import com.company.enterprise.vo.PersonalDashboardVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTodoTest {

    @Mock
    private DashboardMapper dashboardMapper;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void managementDashboardIncludesOnlyNonEmptyTodos() {
        when(dashboardMapper.countPendingApprovals()).thenReturn(2L);
        when(dashboardMapper.countPendingLeave()).thenReturn(2L);
        when(dashboardMapper.countPendingOut()).thenReturn(0L);
        when(dashboardMapper.countTodayAttendanceAnomalies()).thenReturn(1L);
        when(dashboardMapper.countOverduePlans()).thenReturn(0L);
        when(dashboardMapper.countUpcomingBookings()).thenReturn(4L);

        AdminDashboardVO dashboard = new DashboardServiceImpl(dashboardMapper).hrDashboard();

        assertEquals(3, dashboard.getTodos().size());
        assertEquals("pending-leave", dashboard.getTodos().get(0).getKey());
        assertEquals("attendance-anomaly", dashboard.getTodos().get(1).getKey());
        assertEquals(4L, dashboard.getTodos().get(2).getCount());
    }

    @Test
    void personalDashboardUsesCurrentEmployeeAndUserTodoCounts() {
        UserContext.set(new TokenUser(20L, "alice", "Alice", 3L, "RND_ENGINEER", 7L));
        when(dashboardMapper.countTodayAttendanceByEmployeeId(7L)).thenReturn(1L);
        when(dashboardMapper.countPendingLeaveByEmployeeId(7L)).thenReturn(0L);
        when(dashboardMapper.countPendingOutByEmployeeId(7L)).thenReturn(1L);
        when(dashboardMapper.countIncompleteAttendanceByEmployeeId(7L)).thenReturn(1L);
        when(dashboardMapper.countOverduePersonalPlansByEmployeeId(7L)).thenReturn(2L);
        when(dashboardMapper.countInProgressPersonalPlansByEmployeeId(7L)).thenReturn(1L);
        when(dashboardMapper.countUpcomingBookingsByUserId(20L)).thenReturn(3L);

        PersonalDashboardVO dashboard = new DashboardServiceImpl(dashboardMapper).personalDashboard();

        assertEquals(4, dashboard.getTodos().size());
        assertEquals("incomplete-attendance", dashboard.getTodos().get(0).getKey());
        assertEquals("pending-out", dashboard.getTodos().get(1).getKey());
        assertEquals("/plans", dashboard.getTodos().get(2).getTargetPath());
        assertEquals(3L, dashboard.getTodos().get(3).getCount());
    }
}
