package com.company.enterprise.service.impl;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.dto.PlanProgressRequest;
import com.company.enterprise.dto.PlanQuery;
import com.company.enterprise.dto.PlanRequest;
import com.company.enterprise.entity.Plan;
import com.company.enterprise.entity.PlanProgress;
import com.company.enterprise.mapper.PlanMapper;
import com.company.enterprise.mapper.PlanProgressMapper;
import com.company.enterprise.service.PlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlanServiceImpl implements PlanService {

    private final PlanMapper planMapper;
    private final PlanProgressMapper planProgressMapper;

    public PlanServiceImpl(PlanMapper planMapper, PlanProgressMapper planProgressMapper) {
        this.planMapper = planMapper;
        this.planProgressMapper = planProgressMapper;
    }

    @Override
    public PageResult<Plan> page(PlanQuery query) {
        long current = ServiceSupport.normalizeCurrent(query.getCurrent());
        long size = ServiceSupport.normalizeSize(query.getSize());
        long offset = (current - 1) * size;
        Long total = planMapper.countPlanPage(query);
        List<Plan> records = total == null || total == 0
                ? List.of()
                : planMapper.selectPlanPage(query, offset, size);
        return new PageResult<>(total == null ? 0 : total, current, size, records);
    }

    @Override
    public Plan detail(Long id) {
        Plan plan = requirePlan(id);
        requirePlanVisible(plan);
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PlanRequest request) {
        normalizeNonAdminPlanRequest(request);
        validatePlan(request);
        Plan plan = new Plan();
        BeanUtils.copyProperties(request, plan);
        normalize(plan);
        plan.setPlanNo(ServiceSupport.businessNo("PL"));
        plan.setCreatorId(ServiceSupport.requireUserId());
        planMapper.insertPlan(plan);
        return plan.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PlanRequest request) {
        if (request.getId() == null) {
            throw new BusinessException("计划ID不能为空");
        }
        Plan existing = requirePlan(request.getId());
        requirePlanOwner(existing, "只能修改自己的个人计划");
        normalizeNonAdminPlanRequest(request);
        validatePlan(request);
        Plan plan = new Plan();
        BeanUtils.copyProperties(request, plan);
        normalize(plan);
        planMapper.updatePlan(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        if (status == null || status < 0 || status > 4) {
            throw new BusinessException("计划状态必须在0到4之间");
        }
        Plan plan = requirePlan(id);
        requirePlanOwner(plan, "只能更新自己的个人计划状态");
        Integer completedProgress = status == 2 ? 100 : null;
        planMapper.updatePlanStatus(id, status, completedProgress);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProgress(PlanProgressRequest request) {
        Plan plan = requirePlan(request.getPlanId());
        requirePlanOwner(plan, "只能更新自己的个人计划进度");
        int status = request.getProgress() >= 100 ? 2 : 1;
        planMapper.updatePlanProgress(request.getPlanId(), request.getProgress(), status);

        PlanProgress progress = new PlanProgress();
        progress.setPlanId(request.getPlanId());
        progress.setProgress(request.getProgress());
        progress.setContent(request.getContent());
        progress.setSubmitterId(ServiceSupport.requireUserId());
        planProgressMapper.insertPlanProgress(progress);
    }

    @Override
    public List<PlanProgress> progressList(Long planId) {
        Plan plan = requirePlan(planId);
        requirePlanVisible(plan);
        return planProgressMapper.selectByPlanId(planId);
    }

    @Override
    public void delete(Long id) {
        Plan plan = requirePlan(id);
        requirePlanOwner(plan, "只能删除自己的个人计划");
        planMapper.logicDeletePlanById(id);
    }

    private Plan requirePlan(Long id) {
        if (id == null) {
            throw new BusinessException("计划ID不能为空");
        }
        Plan plan = planMapper.selectPlanById(id);
        if (plan == null) {
            throw new BusinessException("计划不存在");
        }
        return plan;
    }

    private void validatePlan(PlanRequest request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
        if (request.getProgress() != null && (request.getProgress() < 0 || request.getProgress() > 100)) {
            throw new BusinessException("计划进度必须在0到100之间");
        }
    }

    private void normalizeNonAdminPlanRequest(PlanRequest request) {
        if (ServiceSupport.canManagePlans()) {
            return;
        }
        request.setPlanType(3);
        request.setDeptId(null);
        request.setEmployeeId(ServiceSupport.requireEmployeeId());
    }

    private void requirePlanVisible(Plan plan) {
        if (ServiceSupport.canManagePlans() || plan.getPlanType() == 1) {
            return;
        }
        ServiceSupport.requireCurrentEmployeeOrAdmin(plan.getEmployeeId(), "只能查看自己的个人计划");
    }

    private void requirePlanOwner(Plan plan, String message) {
        if (ServiceSupport.canManagePlans()) {
            return;
        }
        if (plan.getPlanType() == null || plan.getPlanType() != 3) {
            throw new BusinessException(403, message);
        }
        ServiceSupport.requireCurrentEmployeeOrAdmin(plan.getEmployeeId(), message);
    }

    private void normalize(Plan plan) {
        if (plan.getPriority() == null) {
            plan.setPriority(2);
        }
        if (plan.getProgress() == null) {
            plan.setProgress(0);
        }
        if (plan.getStatus() == null) {
            plan.setStatus(0);
        }
    }
}
