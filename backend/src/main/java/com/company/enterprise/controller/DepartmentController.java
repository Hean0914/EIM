package com.company.enterprise.controller;

import com.company.enterprise.common.Result;
import com.company.enterprise.common.annotation.RequireRoles;
import static com.company.enterprise.common.RoleCodes.GENERAL_MANAGER;
import static com.company.enterprise.common.RoleCodes.HR_MANAGER;
import com.company.enterprise.dto.DepartmentQuery;
import com.company.enterprise.dto.DepartmentSaveRequest;
import com.company.enterprise.dto.DepartmentUpdateRequest;
import com.company.enterprise.entity.Department;
import com.company.enterprise.service.DepartmentService;
import com.company.enterprise.vo.DepartmentTreeVO;
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
@RequestMapping("/departments")
@RequireRoles({GENERAL_MANAGER, HR_MANAGER})
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/tree")
    public Result<List<DepartmentTreeVO>> tree(DepartmentQuery query) {
        return Result.success(departmentService.tree(query));
    }

    @GetMapping
    public Result<List<Department>> list(DepartmentQuery query) {
        return Result.success(departmentService.list(query));
    }

    @GetMapping("/{id}")
    public Result<Department> detail(@PathVariable Long id) {
        return Result.success(departmentService.detail(id));
    }

    @PostMapping
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    public Result<Long> create(@Valid @RequestBody DepartmentSaveRequest request) {
        return Result.success(departmentService.create(request));
    }

    @PutMapping
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    public Result<Void> update(@Valid @RequestBody DepartmentUpdateRequest request) {
        departmentService.update(request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    public Result<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return Result.success();
    }
}
