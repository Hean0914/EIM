package com.company.enterprise.service;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.dto.ApprovalHandleRequest;
import com.company.enterprise.dto.ApprovalQuery;
import com.company.enterprise.dto.LeaveApplyRequest;
import com.company.enterprise.entity.ApprovalRecord;
import com.company.enterprise.entity.LeaveApplication;

import java.util.List;

public interface LeaveApplicationService {

    Long apply(LeaveApplyRequest request);

    PageResult<LeaveApplication> page(ApprovalQuery query);

    LeaveApplication detail(Long id);

    List<ApprovalRecord> approvalRecords(Long id);

    void approve(Long id, ApprovalHandleRequest request);

    void cancel(Long id);

    void delete(Long id);
}
