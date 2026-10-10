package com.company.enterprise.service;

import com.company.enterprise.dto.AttendanceRuleUpdateRequest;
import com.company.enterprise.entity.AttendanceRule;

public interface AttendanceRuleService {

    AttendanceRule currentRule();

    void updateRule(AttendanceRuleUpdateRequest request);
}
