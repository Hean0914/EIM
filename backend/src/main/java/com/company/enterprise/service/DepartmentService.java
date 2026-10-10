package com.company.enterprise.service;

import com.company.enterprise.dto.DepartmentQuery;
import com.company.enterprise.dto.DepartmentSaveRequest;
import com.company.enterprise.dto.DepartmentUpdateRequest;
import com.company.enterprise.entity.Department;
import com.company.enterprise.vo.DepartmentTreeVO;

import java.util.List;

public interface DepartmentService {

    List<DepartmentTreeVO> tree(DepartmentQuery query);

    List<Department> list(DepartmentQuery query);

    Department detail(Long id);

    Long create(DepartmentSaveRequest request);

    void update(DepartmentUpdateRequest request);

    void delete(Long id);
}
