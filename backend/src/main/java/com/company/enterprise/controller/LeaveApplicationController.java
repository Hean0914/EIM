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
import com.company.enterprise.dto.LeaveApplyRequest;
import com.company.enterprise.entity.LeaveApplication;
import com.company.enterprise.entity.ApprovalRecord;
import com.company.enterprise.service.LeaveApplicationService;
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
@RequestMapping("/leaves")
public class LeaveApplicationController {

    private final LeaveApplicationService leaveApplicationService;

    public LeaveApplicationController(LeaveApplicationService leaveApplicationService) {
        this.leaveApplicationService = leaveApplicationService;
    }

    @PostMapping
    @Audit(module = "APPROVAL", action = "APPLY", targetType = "LEAVE")
    public Result<Long> apply(@Valid @RequestBody LeaveApplyRequest request) {
        return Result.success(leaveApplicationService.apply(request));
    }

    @GetMapping("/page")
    public Result<PageResult<LeaveApplication>> page(@Valid ApprovalQuery query) {
        if (!UserContext.canApprove()) {
            query.setEmployeeId(UserContext.requireEmployeeId());
        }
        return Result.success(leaveApplicationService.page(query));
    }

    @GetMapping("/{id}")
    public Result<LeaveApplication> detail(@PathVariable Long id) {
        return Result.success(leaveApplicationService.detail(id));
    }

    @GetMapping("/{id}/approval-records")
    public Result<List<ApprovalRecord>> approvalRecords(@PathVariable Long id) {
        return Result.success(leaveApplicationService.approvalRecords(id));
    }

    @PostMapping("/{id}/approve")
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    @Audit(module = "APPROVAL", action = "HANDLE", targetType = "LEAVE")
    public Result<Void> approve(@PathVariable Long id, @Valid @RequestBody ApprovalHandleRequest request) {
        leaveApplicationService.approve(id, request);
        return Result.success();
    }

    @PostMapping("/{id}/cancel")
    @Audit(module = "APPROVAL", action = "CANCEL", targetType = "LEAVE")
    public Result<Void> cancel(@PathVariable Long id) {
        leaveApplicationService.cancel(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    @Audit(module = "APPROVAL", action = "DELETE", targetType = "LEAVE")
    public Result<Void> delete(@PathVariable Long id) {
        leaveApplicationService.delete(id);
        return Result.success();
    }
}
