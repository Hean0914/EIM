package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.dto.AttendanceCalendarQuery;
import com.company.enterprise.dto.ApprovalQuery;
import com.company.enterprise.entity.OutApplication;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OutApplicationMapper extends BaseMapper<OutApplication> {

    List<OutApplication> selectOutPage(@Param("query") ApprovalQuery query,
                                       @Param("offset") Long offset,
                                       @Param("size") Long size);

    Long countOutPage(@Param("query") ApprovalQuery query);

    List<OutApplication> selectApprovedInRange(@Param("query") AttendanceCalendarQuery query);

    OutApplication selectOutById(@Param("id") Long id);

    int insertOut(OutApplication outApplication);

    int updateOutStatus(OutApplication outApplication);

    int logicDeleteOutById(@Param("id") Long id);
}
