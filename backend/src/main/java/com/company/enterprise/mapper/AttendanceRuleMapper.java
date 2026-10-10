package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.entity.AttendanceRule;

public interface AttendanceRuleMapper extends BaseMapper<AttendanceRule> {

    AttendanceRule selectCurrentRule();

    int updateRule(AttendanceRule rule);
}
