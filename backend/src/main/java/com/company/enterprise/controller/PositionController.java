package com.company.enterprise.controller;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.Result;
import com.company.enterprise.common.annotation.RequireRoles;
import static com.company.enterprise.common.RoleCodes.GENERAL_MANAGER;
import static com.company.enterprise.common.RoleCodes.HR_MANAGER;
import com.company.enterprise.dto.PositionPageQuery;
import com.company.enterprise.dto.PositionSaveRequest;
import com.company.enterprise.dto.PositionUpdateRequest;
import com.company.enterprise.entity.Position;
import com.company.enterprise.service.PositionService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/positions")
@RequireRoles({GENERAL_MANAGER, HR_MANAGER})
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping("/page")
    public Result<PageResult<Position>> page(@Valid PositionPageQuery query) {
        return Result.success(positionService.page(query));
    }

    @GetMapping
    public Result<List<Position>> list(@RequestParam(required = false) Long deptId,
                                      @RequestParam(required = false) Integer status) {
        return Result.success(positionService.list(deptId, status));
    }

    @GetMapping("/{id}")
    public Result<Position> detail(@PathVariable Long id) {
        return Result.success(positionService.detail(id));
    }

    @PostMapping
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    public Result<Long> create(@Valid @RequestBody PositionSaveRequest request) {
        return Result.success(positionService.create(request));
    }

    @PutMapping
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    public Result<Void> update(@Valid @RequestBody PositionUpdateRequest request) {
        positionService.update(request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    public Result<Void> delete(@PathVariable Long id) {
        positionService.delete(id);
        return Result.success();
    }
}
