package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.entity.AttendanceLocation;

public interface AttendanceLocationMapper extends BaseMapper<AttendanceLocation> {

    AttendanceLocation selectCurrentLocation();

    int updateLocation(AttendanceLocation location);
}
