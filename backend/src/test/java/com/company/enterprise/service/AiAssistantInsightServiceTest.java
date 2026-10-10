package com.company.enterprise.service;

import com.company.enterprise.entity.LeaveApplication;
import com.company.enterprise.entity.Plan;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiAssistantInsightServiceTest {

    @Test
    void identifiesOverduePlansAndNearDuePlans() {
        Plan overdue = new Plan();
        overdue.setTitle("逾期计划");
        overdue.setEndDate(LocalDate.of(2026, 9, 25));
        overdue.setStatus(1);
        overdue.setProgress(40);

        Plan nearDue = new Plan();
        nearDue.setTitle("临期计划");
        nearDue.setEndDate(LocalDate.of(2026, 10, 1));
        nearDue.setStatus(1);
        nearDue.setProgress(80);

        AiAssistantInsightService.PlanInsight insight =
                AiAssistantInsightService.analyzePlans(
                        List.of(overdue, nearDue),
                        LocalDate.of(2026, 9, 30));

        assertEquals(1, insight.overdueCount());
        assertEquals(1, insight.nearDueCount());
        assertTrue(insight.summary().contains("逾期计划"));
        assertTrue(insight.summary().contains("临期计划"));
    }

    @Test
    void identifiesAgingPendingApprovals() {
        LeaveApplication application = new LeaveApplication();
        application.setStatus(0);
        application.setCreateTime(LocalDateTime.of(2026, 9, 20, 9, 0));

        AiAssistantInsightService.ApprovalInsight insight =
                AiAssistantInsightService.analyzeApprovals(
                        List.of(application),
                        List.of(),
                        LocalDateTime.of(2026, 9, 30, 9, 0));

        assertEquals(1, insight.pendingCount());
        assertEquals(1, insight.agingCount());
        assertTrue(insight.summary().contains("停留"));
    }
}
