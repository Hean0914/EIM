package com.company.enterprise.service;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.dto.PositionPageQuery;
import com.company.enterprise.dto.PositionSaveRequest;
import com.company.enterprise.dto.PositionUpdateRequest;
import com.company.enterprise.entity.Position;

import java.util.List;

public interface PositionService {

    PageResult<Position> page(PositionPageQuery query);

    List<Position> list(Long deptId, Integer status);

    Position detail(Long id);

    Long create(PositionSaveRequest request);

    void update(PositionUpdateRequest request);

    void delete(Long id);
}
