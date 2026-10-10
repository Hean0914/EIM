package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.dto.AttendanceCalendarQuery;
import com.company.enterprise.dto.ApprovalQuery;
import com.company.enterprise.entity.LeaveApplication;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LeaveApplicationMapper extends BaseMapper<LeaveApplication> {

    List<LeaveApplication> selectLeavePage(@Param("query") ApprovalQuery query,
                                           @Param("offset") Long offset,
                                           @Param("size") Long size);

    Long countLeavePage(@Param("query") ApprovalQuery query);

    List<LeaveApplication> selectApprovedInRange(@Param("query") AttendanceCalendarQuery query);

    LeaveApplication selectLeaveById(@Param("id") Long id);

    int insertLeave(LeaveApplication leaveApplication);

    int updateLeaveStatus(LeaveApplication leaveApplication);

    int logicDeleteLeaveById(@Param("id") Long id);
}
