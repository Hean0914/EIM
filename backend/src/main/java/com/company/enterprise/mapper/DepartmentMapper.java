package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.dto.DepartmentQuery;
import com.company.enterprise.entity.Department;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DepartmentMapper extends BaseMapper<Department> {

    List<Department> selectDepartmentList(@Param("query") DepartmentQuery query);

    Department selectDepartmentById(@Param("id") Long id);

    Long countByDeptCode(@Param("deptCode") String deptCode, @Param("excludeId") Long excludeId);

    Long countChildren(@Param("parentId") Long parentId);

    Long countEmployeeByDeptId(@Param("deptId") Long deptId);

    int insertDepartment(Department department);

    int updateDepartment(Department department);

    int logicDeleteDepartmentById(@Param("id") Long id);
}
