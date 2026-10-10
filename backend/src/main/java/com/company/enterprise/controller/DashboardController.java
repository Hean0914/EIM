package com.company.enterprise.controller;

import com.company.enterprise.common.Result;
import com.company.enterprise.common.annotation.RequireAdmin;
import com.company.enterprise.common.annotation.RequireRoles;
import static com.company.enterprise.common.RoleCodes.GENERAL_MANAGER;
import static com.company.enterprise.common.RoleCodes.HR_MANAGER;
import com.company.enterprise.service.DashboardService;
import com.company.enterprise.vo.AdminDashboardVO;
import com.company.enterprise.vo.PersonalDashboardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/personal")
    public Result<PersonalDashboardVO> personalDashboard() {
        return Result.success(dashboardService.personalDashboard());
    }

    @GetMapping("/hr")
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    public Result<AdminDashboardVO> hrDashboard() {
        return Result.success(dashboardService.hrDashboard());
    }

    @GetMapping("/admin")
    @RequireAdmin
    public Result<AdminDashboardVO> adminDashboard(@RequestParam(defaultValue = "7") int trendDays) {
        return Result.success(dashboardService.adminDashboard(trendDays));
    }
}
