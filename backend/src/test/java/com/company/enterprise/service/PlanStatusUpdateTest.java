package com.company.enterprise.service;

import com.company.enterprise.common.TokenUser;
import com.company.enterprise.common.UserContext;
import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.entity.Plan;
import com.company.enterprise.mapper.PlanMapper;
import com.company.enterprise.mapper.PlanProgressMapper;
import com.company.enterprise.service.impl.PlanServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanStatusUpdateTest {

    @Mock
    private PlanMapper planMapper;

    @Mock
    private PlanProgressMapper planProgressMapper;

    private PlanServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PlanServiceImpl(planMapper, planProgressMapper);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void movingPlanToCompletedSetsProgressToOneHundred() {
        UserContext.set(new TokenUser(1L, "manager", "Manager", 1L, "GENERAL_MANAGER", 1L));
        when(planMapper.selectPlanById(10L)).thenReturn(plan(10L, 3, 7L, 60, 1));

        service.updateStatus(10L, 2);

        verify(planMapper).updatePlanStatus(10L, 2, 100);
    }

    @Test
    void movingPlanToNonCompletedStatusPreservesProgress() {
        UserContext.set(new TokenUser(1L, "manager", "Manager", 1L, "GENERAL_MANAGER", 1L));
        when(planMapper.selectPlanById(10L)).thenReturn(plan(10L, 3, 7L, 60, 1));

        service.updateStatus(10L, 3);

        verify(planMapper).updatePlanStatus(10L, 3, null);
    }

    @Test
    void employeeCannotMoveAnotherEmployeesPlan() {
        UserContext.set(new TokenUser(2L, "employee", "Employee", 3L, "RND_ENGINEER", 8L));
        when(planMapper.selectPlanById(10L)).thenReturn(plan(10L, 3, 7L, 60, 1));

        assertThrows(BusinessException.class, () -> service.updateStatus(10L, 1));

        verify(planMapper, never()).updatePlanStatus(anyLong(), anyInt(), any());
    }

    @Test
    void rejectsStatusOutsideSupportedRange() {
        UserContext.set(new TokenUser(1L, "manager", "Manager", 1L, "GENERAL_MANAGER", 1L));

        assertThrows(BusinessException.class, () -> service.updateStatus(10L, 5));

        verifyNoInteractions(planMapper);
    }

    private Plan plan(Long id, Integer planType, Long employeeId, Integer progress, Integer status) {
        Plan plan = new Plan();
        plan.setId(id);
        plan.setPlanType(planType);
        plan.setEmployeeId(employeeId);
        plan.setProgress(progress);
        plan.setStatus(status);
        return plan;
    }
}
