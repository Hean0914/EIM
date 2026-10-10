package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.entity.ApprovalRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApprovalRecordMapper extends BaseMapper<ApprovalRecord> {

    int insertApprovalRecord(ApprovalRecord approvalRecord);

    List<ApprovalRecord> selectByBusiness(@Param("businessType") String businessType, @Param("businessId") Long businessId);
}
