package com.company.enterprise.service.impl;

import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.dto.DepartmentQuery;
import com.company.enterprise.dto.DepartmentSaveRequest;
import com.company.enterprise.dto.DepartmentUpdateRequest;
import com.company.enterprise.entity.Department;
import com.company.enterprise.mapper.DepartmentMapper;
import com.company.enterprise.service.DepartmentService;
import com.company.enterprise.vo.DepartmentTreeVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    public DepartmentServiceImpl(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    @Override
    public List<DepartmentTreeVO> tree(DepartmentQuery query) {
        List<Department> departments = departmentMapper.selectDepartmentList(query);
        Map<Long, DepartmentTreeVO> nodeMap = new LinkedHashMap<>();
        for (Department department : departments) {
            DepartmentTreeVO node = toTreeNode(department);
            nodeMap.put(node.getId(), node);
        }

        List<DepartmentTreeVO> roots = new ArrayList<>();
        for (DepartmentTreeVO node : nodeMap.values()) {
            Long parentId = node.getParentId();
            DepartmentTreeVO parent = parentId == null ? null : nodeMap.get(parentId);
            if (parentId == null || parentId == 0 || parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    @Override
    public List<Department> list(DepartmentQuery query) {
        return departmentMapper.selectDepartmentList(query);
    }

    @Override
    public Department detail(Long id) {
        return requireDepartment(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DepartmentSaveRequest request) {
        validateDeptCodeUnique(null, request.getDeptCode());
        validateParent(request.getParentId(), null);

        Department department = new Department();
        BeanUtils.copyProperties(request, department);
        normalize(department);
        departmentMapper.insertDepartment(department);
        return department.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DepartmentUpdateRequest request) {
        requireDepartment(request.getId());
        validateDeptCodeUnique(request.getId(), request.getDeptCode());
        validateParent(request.getParentId(), request.getId());

        Department department = new Department();
        BeanUtils.copyProperties(request, department);
        normalize(department);
        departmentMapper.updateDepartment(department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireDepartment(id);
        if (departmentMapper.countChildren(id) > 0) {
            throw new BusinessException("该部门存在下级部门，不能删除");
        }
        if (departmentMapper.countEmployeeByDeptId(id) > 0) {
            throw new BusinessException("该部门存在员工档案，不能删除");
        }
        departmentMapper.logicDeleteDepartmentById(id);
    }

    private Department requireDepartment(Long id) {
        if (id == null) {
            throw new BusinessException("部门ID不能为空");
        }
        Department department = departmentMapper.selectDepartmentById(id);
        if (department == null) {
            throw new BusinessException("部门不存在");
        }
        return department;
    }

    private void validateDeptCodeUnique(Long id, String deptCode) {
        if (departmentMapper.countByDeptCode(deptCode, id) > 0) {
            throw new BusinessException("部门编码已存在");
        }
    }

    private void validateParent(Long parentId, Long currentId) {
        if (parentId == null || parentId == 0) {
            return;
        }
        if (currentId != null && parentId.equals(currentId)) {
            throw new BusinessException("上级部门不能选择当前部门");
        }
        if (departmentMapper.selectDepartmentById(parentId) == null) {
            throw new BusinessException("上级部门不存在");
        }
    }

    private void normalize(Department department) {
        if (department.getParentId() == null) {
            department.setParentId(0L);
        }
        if (department.getSortOrder() == null) {
            department.setSortOrder(0);
        }
        if (department.getStatus() == null) {
            department.setStatus(1);
        }
    }

    private DepartmentTreeVO toTreeNode(Department department) {
        DepartmentTreeVO node = new DepartmentTreeVO();
        BeanUtils.copyProperties(department, node);
        return node;
    }
}
