package com.company.enterprise.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.enterprise.common.PageResult;
import com.company.enterprise.dto.EmployeeAccountRequest;
import com.company.enterprise.dto.EmployeePageQuery;
import com.company.enterprise.dto.EmployeeSaveRequest;
import com.company.enterprise.dto.EmployeeStatusRequest;
import com.company.enterprise.dto.EmployeeUpdateRequest;
import com.company.enterprise.entity.Employee;
import com.company.enterprise.vo.EmployeeAccountRoleVO;
import com.company.enterprise.vo.EmployeeProfileVO;

public interface EmployeeService extends IService<Employee> {

    PageResult<Employee> page(EmployeePageQuery query);

    Employee detail(Long id);

    EmployeeAccountRoleVO accountRole(Long id);

    EmployeeProfileVO profile(Long id);

    Long create(EmployeeSaveRequest request);

    void update(EmployeeUpdateRequest request);

    void delete(Long id);

    void updateStatus(Long id, EmployeeStatusRequest request);

    Long assignAccount(Long id, EmployeeAccountRequest request);
}
