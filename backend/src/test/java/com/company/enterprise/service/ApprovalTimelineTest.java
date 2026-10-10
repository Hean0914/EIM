package com.company.enterprise.service;

import com.company.enterprise.common.TokenUser;
import com.company.enterprise.common.UserContext;
import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.entity.ApprovalRecord;
import com.company.enterprise.entity.LeaveApplication;
import com.company.enterprise.entity.OutApplication;
import com.company.enterprise.mapper.ApprovalRecordMapper;
import com.company.enterprise.mapper.LeaveApplicationMapper;
import com.company.enterprise.mapper.OutApplicationMapper;
import com.company.enterprise.service.impl.LeaveApplicationServiceImpl;
import com.company.enterprise.service.impl.OutApplicationServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalTimelineTest {

    @Mock
    private LeaveApplicationMapper leaveApplicationMapper;

    @Mock
    private OutApplicationMapper outApplicationMapper;

    @Mock
    private ApprovalRecordMapper approvalRecordMapper;

    private LeaveApplicationServiceImpl leaveService;
    private OutApplicationServiceImpl outService;

    @BeforeEach
    void setUp() {
        leaveService = new LeaveApplicationServiceImpl(leaveApplicationMapper, approvalRecordMapper);
        outService = new OutApplicationServiceImpl(outApplicationMapper, approvalRecordMapper);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void employeeCanReadOwnLeaveApprovalRecords() {
        UserContext.set(employee(7L));
        when(leaveApplicationMapper.selectLeaveById(10L)).thenReturn(leave(10L, 7L));
        List<ApprovalRecord> records = List.of(record(1L, "LEAVE", 10L));
        when(approvalRecordMapper.selectByBusiness("LEAVE", 10L)).thenReturn(records);

        assertEquals(records, leaveService.approvalRecords(10L));
        verify(approvalRecordMapper).selectByBusiness("LEAVE", 10L);
    }

    @Test
    void managerCanReadAnyOutApprovalRecords() {
        UserContext.set(new TokenUser(1L, "manager", "Manager", 1L, "GENERAL_MANAGER", 1L));
        when(outApplicationMapper.selectOutById(11L)).thenReturn(out(11L, 99L));
        List<ApprovalRecord> records = List.of(record(2L, "OUT", 11L));
        when(approvalRecordMapper.selectByBusiness("OUT", 11L)).thenReturn(records);

        assertEquals(records, outService.approvalRecords(11L));
        verify(approvalRecordMapper).selectByBusiness("OUT", 11L);
    }

    @Test
    void employeeCannotReadAnotherEmployeesLeaveApprovalRecords() {
        UserContext.set(employee(7L));
        when(leaveApplicationMapper.selectLeaveById(10L)).thenReturn(leave(10L, 99L));

        assertThrows(BusinessException.class, () -> leaveService.approvalRecords(10L));
    }

    private TokenUser employee(Long employeeId) {
        return new TokenUser(2L, "employee", "Employee", 3L, "RND_ENGINEER", employeeId);
    }

    private LeaveApplication leave(Long id, Long employeeId) {
        LeaveApplication application = new LeaveApplication();
        application.setId(id);
        application.setEmployeeId(employeeId);
        return application;
    }

    private OutApplication out(Long id, Long employeeId) {
        OutApplication application = new OutApplication();
        application.setId(id);
        application.setEmployeeId(employeeId);
        return application;
    }

    private ApprovalRecord record(Long id, String type, Long businessId) {
        ApprovalRecord record = new ApprovalRecord();
        record.setId(id);
        record.setBusinessType(type);
        record.setBusinessId(businessId);
        return record;
    }
}
