package com.company.enterprise.service;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.dto.ApprovalHandleRequest;
import com.company.enterprise.dto.ApprovalQuery;
import com.company.enterprise.dto.OutApplyRequest;
import com.company.enterprise.entity.ApprovalRecord;
import com.company.enterprise.entity.OutApplication;

import java.util.List;

public interface OutApplicationService {

    Long apply(OutApplyRequest request);

    PageResult<OutApplication> page(ApprovalQuery query);

    OutApplication detail(Long id);

    List<ApprovalRecord> approvalRecords(Long id);

    void approve(Long id, ApprovalHandleRequest request);

    void cancel(Long id);

    void delete(Long id);
}
