package com.company.enterprise.controller;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.Result;
import com.company.enterprise.common.annotation.RequireRoles;
import static com.company.enterprise.common.RoleCodes.GENERAL_MANAGER;
import static com.company.enterprise.common.RoleCodes.HR_MANAGER;
import com.company.enterprise.dto.EmployeeAccountRequest;
import com.company.enterprise.dto.EmployeePageQuery;
import com.company.enterprise.dto.EmployeeSaveRequest;
import com.company.enterprise.dto.EmployeeStatusRequest;
import com.company.enterprise.dto.EmployeeUpdateRequest;
import com.company.enterprise.entity.Employee;
import com.company.enterprise.service.EmployeeService;
import com.company.enterprise.vo.EmployeeAccountRoleVO;
import com.company.enterprise.vo.EmployeeProfileVO;
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
@RequestMapping("/employees")
@RequireRoles({GENERAL_MANAGER, HR_MANAGER})
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/page")
    public Result<PageResult<Employee>> page(@Valid EmployeePageQuery query) {
        return Result.success(employeeService.page(query));
    }

    @GetMapping("/{id}")
    public Result<Employee> detail(@PathVariable Long id) {
        return Result.success(employeeService.detail(id));
    }

    @GetMapping("/{id}/account-role")
    public Result<EmployeeAccountRoleVO> accountRole(@PathVariable Long id) {
        return Result.success(employeeService.accountRole(id));
    }

    @GetMapping("/{id}/profile")
    public Result<EmployeeProfileVO> profile(@PathVariable Long id) {
        return Result.success(employeeService.profile(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody EmployeeSaveRequest request) {
        return Result.success(employeeService.create(request));
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody EmployeeUpdateRequest request) {
        employeeService.update(request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody EmployeeStatusRequest request) {
        employeeService.updateStatus(id, request);
        return Result.success();
    }

    @PostMapping("/{id}/account")
    public Result<Long> assignAccount(@PathVariable Long id, @Valid @RequestBody EmployeeAccountRequest request) {
        return Result.success(employeeService.assignAccount(id, request));
    }
}
