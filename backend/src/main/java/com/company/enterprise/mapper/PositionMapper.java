package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.dto.PositionPageQuery;
import com.company.enterprise.entity.Position;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PositionMapper extends BaseMapper<Position> {

    List<Position> selectPositionPage(@Param("query") PositionPageQuery query,
                                      @Param("offset") Long offset,
                                      @Param("size") Long size);

    Long countPositionPage(@Param("query") PositionPageQuery query);

    List<Position> selectPositionList(@Param("deptId") Long deptId, @Param("status") Integer status);

    Position selectPositionById(@Param("id") Long id);

    Long countByPositionCode(@Param("positionCode") String positionCode, @Param("excludeId") Long excludeId);

    Long countEmployeeByPositionId(@Param("positionId") Long positionId);

    int insertPosition(Position position);

    int updatePosition(Position position);

    int logicDeletePositionById(@Param("id") Long id);
}
