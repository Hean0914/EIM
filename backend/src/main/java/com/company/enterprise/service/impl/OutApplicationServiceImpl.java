package com.company.enterprise.service.impl;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.dto.ApprovalHandleRequest;
import com.company.enterprise.dto.ApprovalQuery;
import com.company.enterprise.dto.OutApplyRequest;
import com.company.enterprise.entity.ApprovalRecord;
import com.company.enterprise.entity.OutApplication;
import com.company.enterprise.mapper.ApprovalRecordMapper;
import com.company.enterprise.mapper.OutApplicationMapper;
import com.company.enterprise.service.OutApplicationService;
import com.company.enterprise.service.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutApplicationServiceImpl implements OutApplicationService {

    private static final String BUSINESS_TYPE = "OUT";

    private final OutApplicationMapper outApplicationMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final MessageService messageService;

    public OutApplicationServiceImpl(OutApplicationMapper outApplicationMapper, ApprovalRecordMapper approvalRecordMapper) {
        this(outApplicationMapper, approvalRecordMapper, null);
    }

    @Autowired
    public OutApplicationServiceImpl(OutApplicationMapper outApplicationMapper, ApprovalRecordMapper approvalRecordMapper,
                                     MessageService messageService) {
        this.outApplicationMapper = outApplicationMapper;
        this.approvalRecordMapper = approvalRecordMapper;
        this.messageService = messageService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long apply(OutApplyRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        OutApplication application = new OutApplication();
        application.setApplyNo(ServiceSupport.businessNo("OA"));
        application.setEmployeeId(ServiceSupport.requireEmployeeId());
        application.setOutType(request.getOutType() == null ? 1 : request.getOutType());
        application.setDestination(request.getDestination());
        application.setStartTime(request.getStartTime());
        application.setEndTime(request.getEndTime());
        application.setReason(request.getReason());
        application.setStatus(0);
        outApplicationMapper.insertOut(application);
        return application.getId();
    }

    @Override
    public PageResult<OutApplication> page(ApprovalQuery query) {
        long current = ServiceSupport.normalizeCurrent(query.getCurrent());
        long size = ServiceSupport.normalizeSize(query.getSize());
        long offset = (current - 1) * size;
        Long total = outApplicationMapper.countOutPage(query);
        List<OutApplication> records = total == null || total == 0
                ? List.of()
                : outApplicationMapper.selectOutPage(query, offset, size);
        return new PageResult<>(total == null ? 0 : total, current, size, records);
    }

    @Override
    public OutApplication detail(Long id) {
        OutApplication application = requireApplication(id);
        ServiceSupport.requireCurrentEmployeeOrAdmin(application.getEmployeeId(), "只能查看自己的外出申请");
        return application;
    }

    @Override
    public List<ApprovalRecord> approvalRecords(Long id) {
        OutApplication application = detail(id);
        return approvalRecordMapper.selectByBusiness(BUSINESS_TYPE, application.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, ApprovalHandleRequest request) {
        if (request.getResult() == null || (request.getResult() != 1 && request.getResult() != 2)) {
            throw new BusinessException("审批结果不正确");
        }
        OutApplication application = requireApplication(id);
        if (application.getStatus() != 0) {
            throw new BusinessException("该申请不是待审批状态");
        }
        Long approverId = ServiceSupport.requireUserId();
        application.setStatus(request.getResult());
        application.setApproverId(approverId);
        application.setApproveTime(LocalDateTime.now());
        application.setApproveComment(request.getComment());
        outApplicationMapper.updateOutStatus(application);
        insertApprovalRecord(id, approverId, request.getResult(), request.getComment());
        String status = request.getResult() == 1 ? "已通过" : "已驳回";
        publishApprovalMessage(application, id, "外出申请" + status,
                "你的外出申请" + status + (request.getComment() == null ? "。" : "：" + request.getComment()),
                request.getResult() == 1 ? 2 : 3);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        OutApplication application = requireApplication(id);
        ServiceSupport.requireCurrentEmployeeOrAdmin(application.getEmployeeId(), "只能撤销自己的外出申请");
        if (application.getStatus() != 0) {
            throw new BusinessException("只能撤销待审批申请");
        }
        Long userId = ServiceSupport.requireUserId();
        application.setStatus(3);
        application.setApproverId(userId);
        application.setApproveTime(LocalDateTime.now());
        application.setApproveComment("申请人撤销");
        outApplicationMapper.updateOutStatus(application);
        insertApprovalRecord(id, userId, 3, "申请人撤销");
        publishApprovalMessage(application, id, "外出申请已撤销", "你提交的外出申请已撤销。", 2);
    }

    @Override
    public void delete(Long id) {
        requireApplication(id);
        outApplicationMapper.logicDeleteOutById(id);
    }

    private OutApplication requireApplication(Long id) {
        if (id == null) {
            throw new BusinessException("申请ID不能为空");
        }
        OutApplication application = outApplicationMapper.selectOutById(id);
        if (application == null) {
            throw new BusinessException("外出申请不存在");
        }
        return application;
    }

    private void insertApprovalRecord(Long businessId, Long approverId, Integer result, String comment) {
        ApprovalRecord record = new ApprovalRecord();
        record.setBusinessType(BUSINESS_TYPE);
        record.setBusinessId(businessId);
        record.setApproverId(approverId);
        record.setApprovalResult(result);
        record.setApprovalComment(comment);
        record.setApprovalTime(LocalDateTime.now());
        approvalRecordMapper.insertApprovalRecord(record);
    }

    private void publishApprovalMessage(OutApplication application, Long id, String title, String content, Integer priority) {
        if (messageService != null) {
            messageService.publishApprovalMessage(application.getEmployeeId(), BUSINESS_TYPE, id, title, content, "/approvals", priority);
        }
    }
}
