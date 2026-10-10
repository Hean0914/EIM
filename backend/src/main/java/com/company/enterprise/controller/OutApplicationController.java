package com.company.enterprise.controller;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.Result;
import com.company.enterprise.common.UserContext;
import com.company.enterprise.common.annotation.RequireRoles;
import com.company.enterprise.common.annotation.Audit;
import static com.company.enterprise.common.RoleCodes.GENERAL_MANAGER;
import static com.company.enterprise.common.RoleCodes.HR_MANAGER;
import com.company.enterprise.dto.ApprovalHandleRequest;
import com.company.enterprise.dto.ApprovalQuery;
import com.company.enterprise.dto.OutApplyRequest;
import com.company.enterprise.entity.OutApplication;
import com.company.enterprise.entity.ApprovalRecord;
import com.company.enterprise.service.OutApplicationService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/outs")
public class OutApplicationController {

    private final OutApplicationService outApplicationService;

    public OutApplicationController(OutApplicationService outApplicationService) {
        this.outApplicationService = outApplicationService;
    }

    @PostMapping
    @Audit(module = "APPROVAL", action = "APPLY", targetType = "OUT")
    public Result<Long> apply(@Valid @RequestBody OutApplyRequest request) {
        return Result.success(outApplicationService.apply(request));
    }

    @GetMapping("/page")
    public Result<PageResult<OutApplication>> page(@Valid ApprovalQuery query) {
        if (!UserContext.canApprove()) {
            query.setEmployeeId(UserContext.requireEmployeeId());
        }
        return Result.success(outApplicationService.page(query));
    }

    @GetMapping("/{id}")
    public Result<OutApplication> detail(@PathVariable Long id) {
        return Result.success(outApplicationService.detail(id));
    }

    @GetMapping("/{id}/approval-records")
    public Result<List<ApprovalRecord>> approvalRecords(@PathVariable Long id) {
        return Result.success(outApplicationService.approvalRecords(id));
    }

    @PostMapping("/{id}/approve")
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    @Audit(module = "APPROVAL", action = "HANDLE", targetType = "OUT")
    public Result<Void> approve(@PathVariable Long id, @Valid @RequestBody ApprovalHandleRequest request) {
        outApplicationService.approve(id, request);
        return Result.success();
    }

    @PostMapping("/{id}/cancel")
    @Audit(module = "APPROVAL", action = "CANCEL", targetType = "OUT")
    public Result<Void> cancel(@PathVariable Long id) {
        outApplicationService.cancel(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    @Audit(module = "APPROVAL", action = "DELETE", targetType = "OUT")
    public Result<Void> delete(@PathVariable Long id) {
        outApplicationService.delete(id);
        return Result.success();
    }
}
