package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.entity.LoginLog;

public interface LoginLogMapper extends BaseMapper<LoginLog> {

    int insertLoginLog(LoginLog loginLog);
}
