package com.company.enterprise.controller;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.Result;
import com.company.enterprise.common.UserContext;
import com.company.enterprise.common.annotation.RequireRoles;
import com.company.enterprise.common.annotation.Audit;
import static com.company.enterprise.common.RoleCodes.GENERAL_MANAGER;
import static com.company.enterprise.common.RoleCodes.HR_MANAGER;
import com.company.enterprise.dto.NoticeQuery;
import com.company.enterprise.dto.NoticeRequest;
import com.company.enterprise.entity.Notice;
import com.company.enterprise.service.NoticeService;
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

@Validated
@RestController
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping("/page")
    public Result<PageResult<Notice>> page(@Valid NoticeQuery query) {
        if (!UserContext.canManageOffice()) {
            query.setPublishStatus(1);
        }
        return Result.success(noticeService.page(query));
    }

    @GetMapping("/{id}")
    public Result<Notice> detail(@PathVariable Long id) {
        return Result.success(noticeService.detail(id));
    }

    @PostMapping
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    @Audit(module = "NOTICE", action = "CREATE", targetType = "NOTICE")
    public Result<Long> create(@Valid @RequestBody NoticeRequest request) {
        return Result.success(noticeService.create(request));
    }

    @PutMapping
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    @Audit(module = "NOTICE", action = "UPDATE", targetType = "NOTICE")
    public Result<Void> update(@Valid @RequestBody NoticeRequest request) {
        noticeService.update(request);
        return Result.success();
    }

    @PostMapping("/{id}/publish")
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    @Audit(module = "NOTICE", action = "PUBLISH", targetType = "NOTICE")
    public Result<Void> publish(@PathVariable Long id) {
        noticeService.publish(id);
        return Result.success();
    }

    @PostMapping("/{id}/offline")
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    @Audit(module = "NOTICE", action = "OFFLINE", targetType = "NOTICE")
    public Result<Void> offline(@PathVariable Long id) {
        noticeService.offline(id);
        return Result.success();
    }

    @PostMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        noticeService.markRead(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    @Audit(module = "NOTICE", action = "DELETE", targetType = "NOTICE")
    public Result<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return Result.success();
    }
}
