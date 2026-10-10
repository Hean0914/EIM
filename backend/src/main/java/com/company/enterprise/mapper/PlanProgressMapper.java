package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.entity.PlanProgress;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PlanProgressMapper extends BaseMapper<PlanProgress> {

    int insertPlanProgress(PlanProgress planProgress);

    List<PlanProgress> selectByPlanId(@Param("planId") Long planId);
}
