package com.company.enterprise.service.impl;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.dto.PositionPageQuery;
import com.company.enterprise.dto.PositionSaveRequest;
import com.company.enterprise.dto.PositionUpdateRequest;
import com.company.enterprise.entity.Position;
import com.company.enterprise.mapper.DepartmentMapper;
import com.company.enterprise.mapper.PositionMapper;
import com.company.enterprise.service.PositionService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PositionServiceImpl implements PositionService {

    private final PositionMapper positionMapper;
    private final DepartmentMapper departmentMapper;

    public PositionServiceImpl(PositionMapper positionMapper, DepartmentMapper departmentMapper) {
        this.positionMapper = positionMapper;
        this.departmentMapper = departmentMapper;
    }

    @Override
    public PageResult<Position> page(PositionPageQuery query) {
        long current = normalizeCurrent(query.getCurrent());
        long size = normalizeSize(query.getSize());
        long offset = (current - 1) * size;
        Long total = positionMapper.countPositionPage(query);
        List<Position> records = total == null || total == 0
                ? List.of()
                : positionMapper.selectPositionPage(query, offset, size);
        return new PageResult<>(total == null ? 0 : total, current, size, records);
    }

    @Override
    public List<Position> list(Long deptId, Integer status) {
        return positionMapper.selectPositionList(deptId, status);
    }

    @Override
    public Position detail(Long id) {
        return requirePosition(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(PositionSaveRequest request) {
        validateDepartment(request.getDeptId());
        validatePositionCodeUnique(null, request.getPositionCode());

        Position position = new Position();
        BeanUtils.copyProperties(request, position);
        normalize(position);
        positionMapper.insertPosition(position);
        return position.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PositionUpdateRequest request) {
        requirePosition(request.getId());
        validateDepartment(request.getDeptId());
        validatePositionCodeUnique(request.getId(), request.getPositionCode());

        Position position = new Position();
        BeanUtils.copyProperties(request, position);
        normalize(position);
        positionMapper.updatePosition(position);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requirePosition(id);
        if (positionMapper.countEmployeeByPositionId(id) > 0) {
            throw new BusinessException("该职位存在员工档案，不能删除");
        }
        positionMapper.logicDeletePositionById(id);
    }

    private Position requirePosition(Long id) {
        if (id == null) {
            throw new BusinessException("职位ID不能为空");
        }
        Position position = positionMapper.selectPositionById(id);
        if (position == null) {
            throw new BusinessException("职位不存在");
        }
        return position;
    }

    private void validateDepartment(Long deptId) {
        if (deptId != null && departmentMapper.selectDepartmentById(deptId) == null) {
            throw new BusinessException("部门不存在");
        }
    }

    private void validatePositionCodeUnique(Long id, String positionCode) {
        if (positionMapper.countByPositionCode(positionCode, id) > 0) {
            throw new BusinessException("职位编码已存在");
        }
    }

    private void normalize(Position position) {
        if (position.getSortOrder() == null) {
            position.setSortOrder(0);
        }
        if (position.getStatus() == null) {
            position.setStatus(1);
        }
    }

    private long normalizeCurrent(Long current) {
        return current == null || current < 1 ? 1 : current;
    }

    private long normalizeSize(Long size) {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }
}
