package com.company.enterprise.mapper;

import com.company.enterprise.vo.DashboardPieItemVO;
import com.company.enterprise.vo.DashboardTrendPointVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface DashboardMapper {

    Long countEmployees();

    Long countActiveEmployees();

    Long countDepartments();

    Long countTodayAttendance();

    Long countPendingApprovals();

    Long countPendingLeave();

    Long countPendingOut();

    Long countInProgressPlans();

    Long countTodayAttendanceAnomalies();

    Long countOverduePlans();

    Long countUpcomingBookings();

    List<DashboardTrendPointVO> selectAttendanceTrend(@Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    List<DashboardPieItemVO> selectDepartmentEmployeePie();

    Long countTodayAttendanceByEmployeeId(@Param("employeeId") Long employeeId);

    Long countPendingLeaveByEmployeeId(@Param("employeeId") Long employeeId);

    Long countPendingOutByEmployeeId(@Param("employeeId") Long employeeId);

    Long countIncompleteAttendanceByEmployeeId(@Param("employeeId") Long employeeId);

    Long countOverduePersonalPlansByEmployeeId(@Param("employeeId") Long employeeId);

    Long countInProgressPersonalPlansByEmployeeId(@Param("employeeId") Long employeeId);

    Long countUpcomingBookingsByUserId(@Param("userId") Long userId);
}
