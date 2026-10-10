package com.company.enterprise.vo;

import lombok.Data;

import java.util.List;

@Data
public class PersonalDashboardVO {

    private List<DashboardMetricVO> metrics;
    private List<DashboardTodoVO> todos;
}
