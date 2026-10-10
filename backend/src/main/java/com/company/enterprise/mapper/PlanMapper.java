package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.dto.PlanQuery;
import com.company.enterprise.entity.Plan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PlanMapper extends BaseMapper<Plan> {

    List<Plan> selectPlanPage(@Param("query") PlanQuery query,
                              @Param("offset") Long offset,
                              @Param("size") Long size);

    Long countPlanPage(@Param("query") PlanQuery query);

    Plan selectPlanById(@Param("id") Long id);

    int insertPlan(Plan plan);

    int updatePlan(Plan plan);

    int updatePlanStatus(@Param("id") Long id,
                         @Param("status") Integer status,
                         @Param("progress") Integer progress);

    int updatePlanProgress(@Param("id") Long id, @Param("progress") Integer progress, @Param("status") Integer status);

    int logicDeletePlanById(@Param("id") Long id);
}
