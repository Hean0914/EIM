package com.company.enterprise.service;

import com.company.enterprise.vo.AdminDashboardVO;
import com.company.enterprise.vo.PersonalDashboardVO;

public interface DashboardService {

    AdminDashboardVO adminDashboard(int trendDays);

    AdminDashboardVO hrDashboard();

    PersonalDashboardVO personalDashboard();
}
