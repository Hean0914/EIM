package com.company.enterprise.service.impl;

import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.dto.AttendanceRuleUpdateRequest;
import com.company.enterprise.entity.AttendanceRule;
import com.company.enterprise.mapper.AttendanceRuleMapper;
import com.company.enterprise.service.AttendanceRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceRuleServiceImpl implements AttendanceRuleService {

    private final AttendanceRuleMapper attendanceRuleMapper;

    public AttendanceRuleServiceImpl(AttendanceRuleMapper attendanceRuleMapper) {
        this.attendanceRuleMapper = attendanceRuleMapper;
    }

    @Override
    public AttendanceRule currentRule() {
        AttendanceRule rule = attendanceRuleMapper.selectCurrentRule();
        if (rule == null) {
            throw new BusinessException("未配置企业考勤规则");
        }
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(AttendanceRuleUpdateRequest request) {
        if (!request.getWorkEndTime().isAfter(request.getWorkStartTime())) {
            throw new BusinessException("下班打卡时间必须晚于上班打卡时间");
        }
        AttendanceRule rule = currentRule();
        rule.setWorkStartTime(request.getWorkStartTime());
        rule.setWorkEndTime(request.getWorkEndTime());
        rule.setRemark(request.getRemark());
        attendanceRuleMapper.updateRule(rule);
    }
}
