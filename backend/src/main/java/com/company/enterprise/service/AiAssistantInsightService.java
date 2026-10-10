package com.company.enterprise.service;

import com.company.enterprise.entity.LeaveApplication;
import com.company.enterprise.entity.OutApplication;
import com.company.enterprise.entity.Plan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class AiAssistantInsightService {

    private static final int PENDING_STATUS = 0;
    private static final int COMPLETED_STATUS = 2;
    private static final int CANCELLED_STATUS = 4;

    private AiAssistantInsightService() {
    }

    public static PlanInsight analyzePlans(List<Plan> plans, LocalDate today) {
        int overdue = 0;
        int nearDue = 0;
        List<String> lines = new ArrayList<>();
        LocalDate nearDueDate = today.plusDays(2);
        for (Plan plan : plans == null ? List.<Plan>of() : plans) {
            if (plan == null || isClosed(plan.getStatus()) || plan.getEndDate() == null) {
                continue;
            }
            if (plan.getEndDate().isBefore(today) || Integer.valueOf(3).equals(plan.getStatus())) {
                overdue++;
                lines.add("高风险：计划「" + title(plan.getTitle()) + "」已逾期，截止 "
                        + plan.getEndDate() + "，当前进度 " + progress(plan) + "%。");
            } else if (!plan.getEndDate().isAfter(nearDueDate)) {
                nearDue++;
                lines.add("中风险：计划「" + title(plan.getTitle()) + "」将在 "
                        + plan.getEndDate() + " 到期，当前进度 " + progress(plan) + "%。");
            }
        }
        String summary = lines.isEmpty() ? "未发现逾期或两日内到期的未完成计划。" : String.join("\n", lines);
        return new PlanInsight(overdue, nearDue, summary);
    }

    public static ApprovalInsight analyzeApprovals(List<LeaveApplication> leaves,
                                                   List<OutApplication> outs,
                                                   LocalDateTime now) {
        int pending = 0;
        int aging = 0;
        List<String> lines = new ArrayList<>();
        LocalDateTime agingTime = now.minusDays(3);
        List<LocalDateTime> createdTimes = new ArrayList<>();
        if (leaves != null) {
            for (LeaveApplication leave : leaves) {
                if (isPending(leave == null ? null : leave.getStatus())) {
                    pending++;
                    createdTimes.add(leave.getCreateTime());
                }
            }
        }
        if (outs != null) {
            for (OutApplication out : outs) {
                if (isPending(out == null ? null : out.getStatus())) {
                    pending++;
                    createdTimes.add(out.getCreateTime());
                }
            }
        }
        for (LocalDateTime createdTime : createdTimes) {
            if (createdTime != null && createdTime.isBefore(agingTime)) {
                aging++;
            }
        }
        if (pending == 0) {
            lines.add("当前没有待审批申请。");
        } else {
            lines.add("当前有 " + pending + " 条待审批申请。");
            if (aging > 0) {
                lines.add("其中 " + aging + " 条已停留超过 3 天，建议优先处理。");
            } else {
                lines.add("暂未发现停留超过 3 天的申请。");
            }
        }
        return new ApprovalInsight(pending, aging, String.join("\n", lines));
    }

    private static boolean isPending(Integer status) {
        return Integer.valueOf(PENDING_STATUS).equals(status);
    }

    private static boolean isClosed(Integer status) {
        return Integer.valueOf(COMPLETED_STATUS).equals(status)
                || Integer.valueOf(CANCELLED_STATUS).equals(status);
    }

    private static String title(String title) {
        return title == null || title.isBlank() ? "未命名计划" : title;
    }

    private static Integer progress(Plan plan) {
        return plan.getProgress() == null ? 0 : plan.getProgress();
    }

    public record PlanInsight(int overdueCount, int nearDueCount, String summary) {
    }

    public record ApprovalInsight(int pendingCount, int agingCount, String summary) {
    }
}
