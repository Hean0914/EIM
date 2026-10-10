package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.dto.AttendanceStatQuery;
import com.company.enterprise.entity.AttendanceStat;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AttendanceStatMapper extends BaseMapper<AttendanceStat> {

    List<AttendanceStat> selectStatList(@Param("query") AttendanceStatQuery query);
}
