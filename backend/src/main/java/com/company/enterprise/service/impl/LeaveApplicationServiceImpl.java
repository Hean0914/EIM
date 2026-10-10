package com.company.enterprise.service.impl;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.dto.ApprovalHandleRequest;
import com.company.enterprise.dto.ApprovalQuery;
import com.company.enterprise.dto.LeaveApplyRequest;
import com.company.enterprise.entity.ApprovalRecord;
import com.company.enterprise.entity.LeaveApplication;
import com.company.enterprise.mapper.ApprovalRecordMapper;
import com.company.enterprise.mapper.LeaveApplicationMapper;
import com.company.enterprise.service.LeaveApplicationService;
import com.company.enterprise.service.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveApplicationServiceImpl implements LeaveApplicationService {

    private static final String BUSINESS_TYPE = "LEAVE";

    private final LeaveApplicationMapper leaveApplicationMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    private final MessageService messageService;

    public LeaveApplicationServiceImpl(LeaveApplicationMapper leaveApplicationMapper, ApprovalRecordMapper approvalRecordMapper) {
        this(leaveApplicationMapper, approvalRecordMapper, null);
    }

    @Autowired
    public LeaveApplicationServiceImpl(LeaveApplicationMapper leaveApplicationMapper, ApprovalRecordMapper approvalRecordMapper,
                                       MessageService messageService) {
        this.leaveApplicationMapper = leaveApplicationMapper;
        this.approvalRecordMapper = approvalRecordMapper;
        this.messageService = messageService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long apply(LeaveApplyRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        LeaveApplication application = new LeaveApplication();
        application.setApplyNo(ServiceSupport.businessNo("LA"));
        application.setEmployeeId(ServiceSupport.requireEmployeeId());
        application.setLeaveType(request.getLeaveType());
        application.setStartTime(request.getStartTime());
        application.setEndTime(request.getEndTime());
        application.setLeaveDays(request.getLeaveDays());
        application.setReason(request.getReason());
        application.setStatus(0);
        leaveApplicationMapper.insertLeave(application);
        return application.getId();
    }

    @Override
    public PageResult<LeaveApplication> page(ApprovalQuery query) {
        long current = ServiceSupport.normalizeCurrent(query.getCurrent());
        long size = ServiceSupport.normalizeSize(query.getSize());
        long offset = (current - 1) * size;
        Long total = leaveApplicationMapper.countLeavePage(query);
        List<LeaveApplication> records = total == null || total == 0
                ? List.of()
                : leaveApplicationMapper.selectLeavePage(query, offset, size);
        return new PageResult<>(total == null ? 0 : total, current, size, records);
    }

    @Override
    public LeaveApplication detail(Long id) {
        LeaveApplication application = requireApplication(id);
        ServiceSupport.requireCurrentEmployeeOrAdmin(application.getEmployeeId(), "只能查看自己的请假申请");
        return application;
    }

    @Override
    public List<ApprovalRecord> approvalRecords(Long id) {
        LeaveApplication application = detail(id);
        return approvalRecordMapper.selectByBusiness(BUSINESS_TYPE, application.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, ApprovalHandleRequest request) {
        if (request.getResult() == null || (request.getResult() != 1 && request.getResult() != 2)) {
            throw new BusinessException("审批结果不正确");
        }
        LeaveApplication application = requireApplication(id);
        if (application.getStatus() != 0) {
            throw new BusinessException("该申请不是待审批状态");
        }
        Long approverId = ServiceSupport.requireUserId();
        application.setStatus(request.getResult());
        application.setApproverId(approverId);
        application.setApproveTime(LocalDateTime.now());
        application.setApproveComment(request.getComment());
        leaveApplicationMapper.updateLeaveStatus(application);
        insertApprovalRecord(id, approverId, request.getResult(), request.getComment());
        String status = request.getResult() == 1 ? "已通过" : "已驳回";
        publishApprovalMessage(application, id, "请假申请" + status,
                "你的请假申请" + status + (request.getComment() == null ? "。" : "：" + request.getComment()),
                request.getResult() == 1 ? 2 : 3);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        LeaveApplication application = requireApplication(id);
        ServiceSupport.requireCurrentEmployeeOrAdmin(application.getEmployeeId(), "只能撤销自己的请假申请");
        if (application.getStatus() != 0) {
            throw new BusinessException("只能撤销待审批申请");
        }
        Long userId = ServiceSupport.requireUserId();
        application.setStatus(3);
        application.setApproverId(userId);
        application.setApproveTime(LocalDateTime.now());
        application.setApproveComment("申请人撤销");
        leaveApplicationMapper.updateLeaveStatus(application);
        insertApprovalRecord(id, userId, 3, "申请人撤销");
        publishApprovalMessage(application, id, "请假申请已撤销", "你提交的请假申请已撤销。", 2);
    }

    @Override
    public void delete(Long id) {
        requireApplication(id);
        leaveApplicationMapper.logicDeleteLeaveById(id);
    }

    private LeaveApplication requireApplication(Long id) {
        if (id == null) {
            throw new BusinessException("申请ID不能为空");
        }
        LeaveApplication application = leaveApplicationMapper.selectLeaveById(id);
        if (application == null) {
            throw new BusinessException("请假申请不存在");
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

    private void publishApprovalMessage(LeaveApplication application, Long id, String title, String content, Integer priority) {
        if (messageService != null) {
            messageService.publishApprovalMessage(application.getEmployeeId(), BUSINESS_TYPE, id, title, content, "/approvals", priority);
        }
    }
}
