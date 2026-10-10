package com.company.enterprise.controller;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.Result;
import com.company.enterprise.common.UserContext;
import com.company.enterprise.common.annotation.RequireAdmin;
import com.company.enterprise.dto.AttendanceLocationUpdateRequest;
import com.company.enterprise.dto.AttendanceCalendarQuery;
import com.company.enterprise.dto.AttendanceRecordQuery;
import com.company.enterprise.dto.AttendanceRuleUpdateRequest;
import com.company.enterprise.dto.AttendanceStatQuery;
import com.company.enterprise.dto.ClockRequest;
import com.company.enterprise.entity.AttendanceLocation;
import com.company.enterprise.entity.AttendanceRecord;
import com.company.enterprise.entity.AttendanceRule;
import com.company.enterprise.entity.AttendanceStat;
import com.company.enterprise.service.AttendanceRuleService;
import com.company.enterprise.service.AttendanceLocationService;
import com.company.enterprise.service.AttendanceService;
import com.company.enterprise.vo.AttendanceAnalysisResult;
import com.company.enterprise.vo.AttendanceCalendarItem;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceRuleService attendanceRuleService;
    private final AttendanceLocationService attendanceLocationService;

    public AttendanceController(AttendanceService attendanceService, AttendanceRuleService attendanceRuleService,
                                AttendanceLocationService attendanceLocationService) {
        this.attendanceService = attendanceService;
        this.attendanceRuleService = attendanceRuleService;
        this.attendanceLocationService = attendanceLocationService;
    }

    @GetMapping("/location")
    public Result<AttendanceLocation> currentLocation() {
        return Result.success(attendanceLocationService.currentLocation());
    }

    @PutMapping("/location")
    @RequireAdmin
    public Result<Void> updateLocation(@Valid @RequestBody AttendanceLocationUpdateRequest request) {
        attendanceLocationService.updateLocation(request);
        return Result.success();
    }

    @GetMapping("/rule")
    public Result<AttendanceRule> currentRule() {
        return Result.success(attendanceRuleService.currentRule());
    }

    @PutMapping("/rule")
    @RequireAdmin
    public Result<Void> updateRule(@Valid @RequestBody AttendanceRuleUpdateRequest request) {
        attendanceRuleService.updateRule(request);
        return Result.success();
    }

    @PostMapping("/check-in")
    public Result<AttendanceRecord> checkIn(@Valid @RequestBody ClockRequest request) {
        return Result.success(attendanceService.checkIn(request));
    }

    @PostMapping("/check-out")
    public Result<AttendanceRecord> checkOut(@Valid @RequestBody ClockRequest request) {
        return Result.success(attendanceService.checkOut(request));
    }

    @GetMapping("/records/page")
    public Result<PageResult<AttendanceRecord>> page(@Valid AttendanceRecordQuery query) {
        if (!UserContext.canManagePeople()) {
            query.setEmployeeId(UserContext.requireEmployeeId());
        }
        return Result.success(attendanceService.page(query));
    }

    @GetMapping("/stats")
    public Result<List<AttendanceStat>> statList(AttendanceStatQuery query) {
        if (!UserContext.canManagePeople()) {
            query.setEmployeeId(UserContext.requireEmployeeId());
        }
        return Result.success(attendanceService.statList(query));
    }

    @GetMapping("/calendar")
    public Result<List<AttendanceCalendarItem>> calendar(@Valid AttendanceCalendarQuery query) {
        applyEmployeeScope(query);
        return Result.success(attendanceService.calendar(query));
    }

    @GetMapping("/analysis")
    public Result<AttendanceAnalysisResult> analysis(@Valid AttendanceCalendarQuery query) {
        applyEmployeeScope(query);
        return Result.success(attendanceService.analysis(query));
    }

    private void applyEmployeeScope(AttendanceCalendarQuery query) {
        if (!UserContext.canManagePeople()) {
            query.setEmployeeId(UserContext.requireEmployeeId());
        }
    }
}
