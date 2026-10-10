package com.company.enterprise.service.impl;

import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.dto.AttendanceLocationUpdateRequest;
import com.company.enterprise.entity.AttendanceLocation;
import com.company.enterprise.mapper.AttendanceLocationMapper;
import com.company.enterprise.service.AttendanceLocationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceLocationServiceImpl implements AttendanceLocationService {

    private final AttendanceLocationMapper attendanceLocationMapper;

    public AttendanceLocationServiceImpl(AttendanceLocationMapper attendanceLocationMapper) {
        this.attendanceLocationMapper = attendanceLocationMapper;
    }

    @Override
    public AttendanceLocation currentLocation() {
        AttendanceLocation location = attendanceLocationMapper.selectCurrentLocation();
        if (location == null) {
            throw new BusinessException("未配置企业GPS打卡地点");
        }
        return location;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLocation(AttendanceLocationUpdateRequest request) {
        AttendanceLocation location = attendanceLocationMapper.selectCurrentLocation();
        if (location == null) {
            location = new AttendanceLocation();
            location.setStatus(1);
            location.setLocationName(request.getLocationName());
            location.setAddress(request.getAddress());
            location.setLongitude(request.getLongitude());
            location.setLatitude(request.getLatitude());
            location.setAllowedRadius(request.getAllowedRadius());
            location.setAccuracyLimit(request.getAccuracyLimit());
            location.setRemark(request.getRemark());
            attendanceLocationMapper.insert(location);
            return;
        }
        location.setLocationName(request.getLocationName());
        location.setAddress(request.getAddress());
        location.setLongitude(request.getLongitude());
        location.setLatitude(request.getLatitude());
        location.setAllowedRadius(request.getAllowedRadius());
        location.setAccuracyLimit(request.getAccuracyLimit());
        location.setRemark(request.getRemark());
        attendanceLocationMapper.updateLocation(location);
    }
}
