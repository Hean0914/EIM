package com.company.enterprise.controller;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.Result;
import com.company.enterprise.common.UserContext;
import com.company.enterprise.common.annotation.Audit;
import com.company.enterprise.dto.PlanProgressRequest;
import com.company.enterprise.dto.PlanQuery;
import com.company.enterprise.dto.PlanRequest;
import com.company.enterprise.dto.PlanStatusUpdateRequest;
import com.company.enterprise.entity.Plan;
import com.company.enterprise.entity.PlanProgress;
import com.company.enterprise.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping("/page")
    public Result<PageResult<Plan>> page(@Valid PlanQuery query) {
        if (!UserContext.canManagePlans()) {
            query.setEmployeeId(UserContext.requireEmployeeId());
            query.setIncludeCompanyPlan(true);
        }
        return Result.success(planService.page(query));
    }

    @GetMapping("/{id}")
    public Result<Plan> detail(@PathVariable Long id) {
        return Result.success(planService.detail(id));
    }

    @PostMapping
    @Audit(module = "PLAN", action = "CREATE", targetType = "PLAN")
    public Result<Long> create(@Valid @RequestBody PlanRequest request) {
        return Result.success(planService.create(request));
    }

    @PutMapping
    @Audit(module = "PLAN", action = "UPDATE", targetType = "PLAN")
    public Result<Void> update(@Valid @RequestBody PlanRequest request) {
        planService.update(request);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Audit(module = "PLAN", action = "STATUS", targetType = "PLAN")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @Valid @RequestBody PlanStatusUpdateRequest request) {
        planService.updateStatus(id, request.getStatus());
        return Result.success();
    }

    @PostMapping("/progress")
    @Audit(module = "PLAN", action = "PROGRESS", targetType = "PLAN")
    public Result<Void> updateProgress(@Valid @RequestBody PlanProgressRequest request) {
        planService.updateProgress(request);
        return Result.success();
    }

    @GetMapping("/{id}/progress")
    public Result<List<PlanProgress>> progressList(@PathVariable Long id) {
        return Result.success(planService.progressList(id));
    }

    @DeleteMapping("/{id}")
    @Audit(module = "PLAN", action = "DELETE", targetType = "PLAN")
    public Result<Void> delete(@PathVariable Long id) {
        planService.delete(id);
        return Result.success();
    }
}
