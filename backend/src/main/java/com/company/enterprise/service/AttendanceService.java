package com.company.enterprise.service;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.dto.AttendanceCalendarQuery;
import com.company.enterprise.dto.AttendanceRecordQuery;
import com.company.enterprise.dto.AttendanceStatQuery;
import com.company.enterprise.dto.ClockRequest;
import com.company.enterprise.entity.AttendanceRecord;
import com.company.enterprise.entity.AttendanceStat;
import com.company.enterprise.vo.AttendanceAnalysisResult;
import com.company.enterprise.vo.AttendanceCalendarItem;

import java.util.List;

public interface AttendanceService {

    AttendanceRecord checkIn(ClockRequest request);

    AttendanceRecord checkOut(ClockRequest request);

    PageResult<AttendanceRecord> page(AttendanceRecordQuery query);

    List<AttendanceStat> statList(AttendanceStatQuery query);

    List<AttendanceCalendarItem> calendar(AttendanceCalendarQuery query);

    AttendanceAnalysisResult analysis(AttendanceCalendarQuery query);
}
