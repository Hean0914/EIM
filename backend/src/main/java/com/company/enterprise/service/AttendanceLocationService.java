package com.company.enterprise.service;

import com.company.enterprise.dto.AttendanceLocationUpdateRequest;
import com.company.enterprise.entity.AttendanceLocation;

public interface AttendanceLocationService {

    AttendanceLocation currentLocation();

    void updateLocation(AttendanceLocationUpdateRequest request);
}
