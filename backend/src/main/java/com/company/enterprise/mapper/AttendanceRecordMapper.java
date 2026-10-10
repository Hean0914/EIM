package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.dto.AttendanceCalendarQuery;
import com.company.enterprise.dto.AttendanceRecordQuery;
import com.company.enterprise.entity.AttendanceRecord;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecord> {

    AttendanceRecord selectByEmployeeAndDate(@Param("employeeId") Long employeeId, @Param("attendanceDate") LocalDate attendanceDate);

    List<AttendanceRecord> selectAttendancePage(@Param("query") AttendanceRecordQuery query,
                                                @Param("offset") Long offset,
                                                @Param("size") Long size);

    Long countAttendancePage(@Param("query") AttendanceRecordQuery query);

    List<AttendanceRecord> selectAttendanceRange(@Param("query") AttendanceCalendarQuery query);

    int insertAttendanceRecord(AttendanceRecord record);

    int updateAttendanceRecord(AttendanceRecord record);
}
