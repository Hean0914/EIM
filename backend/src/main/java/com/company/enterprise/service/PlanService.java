package com.company.enterprise.service;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.dto.PlanProgressRequest;
import com.company.enterprise.dto.PlanQuery;
import com.company.enterprise.dto.PlanRequest;
import com.company.enterprise.entity.Plan;
import com.company.enterprise.entity.PlanProgress;

import java.util.List;

public interface PlanService {

    PageResult<Plan> page(PlanQuery query);

    Plan detail(Long id);

    Long create(PlanRequest request);

    void update(PlanRequest request);

    void updateStatus(Long id, Integer status);

    void updateProgress(PlanProgressRequest request);

    List<PlanProgress> progressList(Long planId);

    void delete(Long id);
}
