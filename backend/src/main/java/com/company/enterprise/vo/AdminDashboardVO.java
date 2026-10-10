package com.company.enterprise.vo;

import lombok.Data;

import java.util.List;

@Data
public class AdminDashboardVO {

    private List<DashboardMetricVO> metrics;
    private List<DashboardTrendPointVO> attendanceTrend;
    private List<DashboardPieItemVO> departmentPie;
    private List<DashboardTodoVO> todos;
}
