package com.company.enterprise.service.impl;

import com.company.enterprise.common.UserContext;
import com.company.enterprise.mapper.DashboardMapper;
import com.company.enterprise.service.DashboardService;
import com.company.enterprise.service.DashboardTodoAssembler;
import com.company.enterprise.vo.AdminDashboardVO;
import com.company.enterprise.vo.DashboardMetricVO;
import com.company.enterprise.vo.DashboardPieItemVO;
import com.company.enterprise.vo.DashboardTrendPointVO;
import com.company.enterprise.vo.PersonalDashboardVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter LABEL_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    private final DashboardMapper dashboardMapper;

    public DashboardServiceImpl(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    @Override
    public AdminDashboardVO adminDashboard(int trendDays) {
        return buildDashboard(true, normalizeTrendDays(trendDays));
    }

    @Override
    public AdminDashboardVO hrDashboard() {
        return buildDashboard(false, 7);
    }

    @Override
    public PersonalDashboardVO personalDashboard() {
        Long employeeId = UserContext.requireEmployeeId();
        Long userId = UserContext.requireUserId();
        Long pendingLeave = valueOf(dashboardMapper.countPendingLeaveByEmployeeId(employeeId));
        Long pendingOut = valueOf(dashboardMapper.countPendingOutByEmployeeId(employeeId));
        Long incompleteAttendance = valueOf(dashboardMapper.countIncompleteAttendanceByEmployeeId(employeeId));
        Long overduePlans = valueOf(dashboardMapper.countOverduePersonalPlansByEmployeeId(employeeId));
        Long upcomingMeetings = valueOf(dashboardMapper.countUpcomingBookingsByUserId(userId));
        PersonalDashboardVO dashboard = new PersonalDashboardVO();
        dashboard.setMetrics(List.of(
                new DashboardMetricVO("今日打卡", valueOf(dashboardMapper.countTodayAttendanceByEmployeeId(employeeId)), "次", "本人今日考勤记录", "success"),
                new DashboardMetricVO("待审批请假", pendingLeave, "项", "等待审批的请假申请", "warning"),
                new DashboardMetricVO("待审批外出", pendingOut, "项", "等待审批的外出登记", "warning"),
                new DashboardMetricVO("进行中计划", valueOf(dashboardMapper.countInProgressPersonalPlansByEmployeeId(employeeId)), "项", "本人进行中的个人计划", "info"),
                new DashboardMetricVO("待开始会议", upcomingMeetings, "项", "本人待开始的会议预约", "info")
        ));
        dashboard.setTodos(DashboardTodoAssembler.personalTodos(
                pendingLeave, pendingOut, incompleteAttendance, overduePlans, upcomingMeetings
        ));
        return dashboard;
    }

    private AdminDashboardVO buildDashboard(boolean includePlanMetric, int trendDays) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(trendDays - 1L);
        AdminDashboardVO dashboard = new AdminDashboardVO();
        dashboard.setMetrics(buildMetrics(includePlanMetric));
        dashboard.setAttendanceTrend(fillAttendanceTrend(startDate, endDate));
        dashboard.setDepartmentPie(defaultList(dashboardMapper.selectDepartmentEmployeePie()));
        dashboard.setTodos(DashboardTodoAssembler.managementTodos(
                valueOf(dashboardMapper.countPendingLeave()),
                valueOf(dashboardMapper.countPendingOut()),
                valueOf(dashboardMapper.countTodayAttendanceAnomalies()),
                valueOf(dashboardMapper.countOverduePlans()),
                valueOf(dashboardMapper.countUpcomingBookings())
        ));
        return dashboard;
    }

    private int normalizeTrendDays(int trendDays) {
        return trendDays == 14 || trendDays == 30 ? trendDays : 7;
    }

    private List<DashboardMetricVO> buildMetrics(boolean includePlanMetric) {
        List<DashboardMetricVO> metrics = new java.util.ArrayList<>(List.of(
                new DashboardMetricVO("员工总数", valueOf(dashboardMapper.countEmployees()), "人", "系统员工档案总量", "info"),
                new DashboardMetricVO("在职员工", valueOf(dashboardMapper.countActiveEmployees()), "人", "在职和试用员工", "success"),
                new DashboardMetricVO("启用部门", valueOf(dashboardMapper.countDepartments()), "个", "当前启用组织节点", "info"),
                new DashboardMetricVO("今日打卡", valueOf(dashboardMapper.countTodayAttendance()), "次", "今日已产生考勤记录", "success"),
                new DashboardMetricVO("待审批", valueOf(dashboardMapper.countPendingApprovals()), "项", "请假和外出待处理", "warning")
        ));
        if (includePlanMetric) {
            metrics.add(new DashboardMetricVO("进行中计划", valueOf(dashboardMapper.countInProgressPlans()), "项", "状态为进行中的计划", "info"));
        }
        return metrics;
    }

    private List<DashboardTrendPointVO> fillAttendanceTrend(LocalDate startDate, LocalDate endDate) {
        Map<String, DashboardTrendPointVO> dataMap = defaultList(dashboardMapper.selectAttendanceTrend(startDate, endDate))
                .stream()
                .collect(Collectors.toMap(DashboardTrendPointVO::getDate, Function.identity(), (left, right) -> left));

        long days = endDate.toEpochDay() - startDate.toEpochDay();
        return IntStream.rangeClosed(0, (int) days)
                .mapToObj(startDate::plusDays)
                .map(date -> {
                    String key = date.format(DATE_FORMATTER);
                    DashboardTrendPointVO point = dataMap.get(key);
                    if (point != null) {
                        return point;
                    }
                    return new DashboardTrendPointVO(key, date.format(LABEL_FORMATTER), 0L);
                })
                .toList();
    }

    private Long valueOf(Long value) {
        return value == null ? 0L : value;
    }

    private <T> List<T> defaultList(List<T> records) {
        return records == null ? List.of() : records;
    }
}
