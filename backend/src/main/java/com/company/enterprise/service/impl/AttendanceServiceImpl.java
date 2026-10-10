package com.company.enterprise.service.impl;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.dto.AttendanceCalendarQuery;
import com.company.enterprise.dto.AttendanceRecordQuery;
import com.company.enterprise.dto.AttendanceStatQuery;
import com.company.enterprise.dto.ClockRequest;
import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.entity.AttendanceLocation;
import com.company.enterprise.entity.AttendanceRecord;
import com.company.enterprise.entity.AttendanceRule;
import com.company.enterprise.entity.AttendanceStat;
import com.company.enterprise.mapper.AttendanceRecordMapper;
import com.company.enterprise.mapper.AttendanceStatMapper;
import com.company.enterprise.mapper.EmployeeMapper;
import com.company.enterprise.mapper.LeaveApplicationMapper;
import com.company.enterprise.mapper.OutApplicationMapper;
import com.company.enterprise.service.AttendanceRuleService;
import com.company.enterprise.service.AttendanceLocationService;
import com.company.enterprise.service.AttendanceAnalysisCalculator;
import com.company.enterprise.service.AttendanceService;
import com.company.enterprise.utils.GeoUtils;
import com.company.enterprise.vo.AttendanceAnalysisResult;
import com.company.enterprise.vo.AttendanceCalendarItem;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRecordMapper attendanceRecordMapper;
    private final AttendanceStatMapper attendanceStatMapper;
    private final EmployeeMapper employeeMapper;
    private final LeaveApplicationMapper leaveApplicationMapper;
    private final OutApplicationMapper outApplicationMapper;
    private final AttendanceRuleService attendanceRuleService;
    private final AttendanceLocationService attendanceLocationService;

    public AttendanceServiceImpl(AttendanceRecordMapper attendanceRecordMapper, AttendanceStatMapper attendanceStatMapper,
                                 EmployeeMapper employeeMapper, LeaveApplicationMapper leaveApplicationMapper,
                                 OutApplicationMapper outApplicationMapper,
                                 AttendanceRuleService attendanceRuleService,
                                 AttendanceLocationService attendanceLocationService) {
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.attendanceStatMapper = attendanceStatMapper;
        this.employeeMapper = employeeMapper;
        this.leaveApplicationMapper = leaveApplicationMapper;
        this.outApplicationMapper = outApplicationMapper;
        this.attendanceRuleService = attendanceRuleService;
        this.attendanceLocationService = attendanceLocationService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceRecord checkIn(ClockRequest request) {
        Long employeeId = ServiceSupport.requireEmployeeId();
        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRecordMapper.selectByEmployeeAndDate(employeeId, today);
        AttendanceRule rule = attendanceRuleService.currentRule();
        AttendanceLocation location = attendanceLocationService.currentLocation();
        GpsValidation validation = validateGps(request, location);
        LocalDateTime now = LocalDateTime.now();
        if (record == null) {
            record = new AttendanceRecord();
            record.setEmployeeId(employeeId);
            record.setAttendanceDate(today);
            record.setCheckOutStatus(0);
            record.setWorkMinutes(0);
        }
        record.setCheckInTime(now);
        record.setCheckInStatus(now.toLocalTime().isAfter(rule.getWorkStartTime()) ? 2 : 1);
        record.setCheckInLocation(request.getAddress());
        record.setCheckInLongitude(request.getLongitude());
        record.setCheckInLatitude(request.getLatitude());
        record.setCheckInAccuracy(request.getAccuracy());
        record.setCheckInDistance(validation.distance());
        record.setCheckInGpsStatus(1);
        record.setRemark(request.getRemark());
        if (record.getId() == null) {
            attendanceRecordMapper.insertAttendanceRecord(record);
        } else {
            attendanceRecordMapper.updateAttendanceRecord(record);
        }
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceRecord checkOut(ClockRequest request) {
        Long employeeId = ServiceSupport.requireEmployeeId();
        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRecordMapper.selectByEmployeeAndDate(employeeId, today);
        AttendanceRule rule = attendanceRuleService.currentRule();
        AttendanceLocation location = attendanceLocationService.currentLocation();
        GpsValidation validation = validateGps(request, location);
        LocalDateTime now = LocalDateTime.now();
        if (record == null) {
            record = new AttendanceRecord();
            record.setEmployeeId(employeeId);
            record.setAttendanceDate(today);
            record.setCheckInStatus(0);
            record.setWorkMinutes(0);
        }
        record.setCheckOutTime(now);
        record.setCheckOutStatus(now.toLocalTime().isBefore(rule.getWorkEndTime()) ? 2 : 1);
        record.setCheckOutLocation(request.getAddress());
        record.setCheckOutLongitude(request.getLongitude());
        record.setCheckOutLatitude(request.getLatitude());
        record.setCheckOutAccuracy(request.getAccuracy());
        record.setCheckOutDistance(validation.distance());
        record.setCheckOutGpsStatus(1);
        record.setRemark(request.getRemark());
        if (record.getCheckInTime() != null) {
            record.setWorkMinutes((int) Duration.between(record.getCheckInTime(), now).toMinutes());
        }
        if (record.getId() == null) {
            attendanceRecordMapper.insertAttendanceRecord(record);
        } else {
            attendanceRecordMapper.updateAttendanceRecord(record);
        }
        return record;
    }

    private GpsValidation validateGps(ClockRequest request, AttendanceLocation location) {
        if (request == null || request.getLongitude() == null || request.getLatitude() == null
                || request.getAccuracy() == null) {
            throw new BusinessException("无法获取GPS定位，请开启定位权限后重试");
        }
        if (request.getAccuracy().compareTo(BigDecimal.valueOf(location.getAccuracyLimit())) > 0) {
            throw new BusinessException("当前定位精度不足，请到开阔区域后重试");
        }
        BigDecimal distance = GeoUtils.distanceMeters(
                request.getLatitude(), request.getLongitude(),
                location.getLatitude(), location.getLongitude()
        );
        if (distance.compareTo(BigDecimal.valueOf(location.getAllowedRadius())) > 0) {
            throw new BusinessException("当前位置不在允许打卡范围内");
        }
        return new GpsValidation(distance);
    }

    private record GpsValidation(BigDecimal distance) {
    }

    @Override
    public PageResult<AttendanceRecord> page(AttendanceRecordQuery query) {
        long current = ServiceSupport.normalizeCurrent(query.getCurrent());
        long size = ServiceSupport.normalizeSize(query.getSize());
        long offset = (current - 1) * size;
        Long total = attendanceRecordMapper.countAttendancePage(query);
        List<AttendanceRecord> records = total == null || total == 0
                ? List.of()
                : attendanceRecordMapper.selectAttendancePage(query, offset, size);
        return new PageResult<>(total == null ? 0 : total, current, size, records);
    }

    @Override
    public List<AttendanceStat> statList(AttendanceStatQuery query) {
        return attendanceStatMapper.selectStatList(query);
    }

    @Override
    public List<AttendanceCalendarItem> calendar(AttendanceCalendarQuery query) {
        return analysis(query).getCalendar();
    }

    @Override
    public AttendanceAnalysisResult analysis(AttendanceCalendarQuery query) {
        if (query.getStartDate() == null || query.getEndDate() == null
                || query.getEndDate().isBefore(query.getStartDate())) {
            throw new BusinessException("考勤分析日期范围无效");
        }
        LambdaQueryWrapper<com.company.enterprise.entity.Employee> employeeQuery =
                new LambdaQueryWrapper<com.company.enterprise.entity.Employee>()
                        .eq(com.company.enterprise.entity.Employee::getEmploymentStatus, 1)
                        .orderByAsc(com.company.enterprise.entity.Employee::getId);
        if (query.getEmployeeId() != null) {
            employeeQuery.eq(com.company.enterprise.entity.Employee::getId, query.getEmployeeId());
        }
        List<com.company.enterprise.entity.Employee> employees = employeeMapper.selectList(employeeQuery);
        AttendanceRule rule = attendanceRuleService.currentRule();
        return AttendanceAnalysisCalculator.calculate(
                query.getStartDate(),
                query.getEndDate(),
                employees,
                attendanceRecordMapper.selectAttendanceRange(query),
                leaveApplicationMapper.selectApprovedInRange(query),
                outApplicationMapper.selectApprovedInRange(query),
                rule == null ? null : rule.getWorkStartTime(),
                rule == null ? null : rule.getWorkEndTime()
        );
    }
}
