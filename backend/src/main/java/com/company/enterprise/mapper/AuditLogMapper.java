package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.dto.AuditLogQuery;
import com.company.enterprise.entity.AuditLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AuditLogMapper extends BaseMapper<AuditLog> {

    List<AuditLog> selectPage(@Param("query") AuditLogQuery query,
                              @Param("offset") Long offset,
                              @Param("size") Long size);

    Long countPage(@Param("query") AuditLogQuery query);

    Long countByStatus(@Param("query") AuditLogQuery query, @Param("operationStatus") Integer operationStatus);

    int insertAuditLog(AuditLog auditLog);
}
