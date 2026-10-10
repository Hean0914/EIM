package com.company.enterprise.service;

import com.company.enterprise.vo.DashboardTodoVO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DashboardTodoAssembler {

    private DashboardTodoAssembler() {
    }

    public static List<DashboardTodoVO> managementTodos(long pendingLeave, long pendingOut,
                                                          long attendanceAnomaly, long overduePlans,
                                                          long upcomingMeetings) {
        List<DashboardTodoVO> todos = new ArrayList<>();
        add(todos, pendingLeave, "pending-leave", "approval", "待审批请假",
                "有 %d 项请假申请等待处理", "high", "warning", "/approvals");
        add(todos, pendingOut, "pending-out", "approval", "待审批外出",
                "有 %d 项外出申请等待处理", "high", "warning", "/approvals");
        add(todos, attendanceAnomaly, "attendance-anomaly", "attendance", "今日考勤异常",
                "发现 %d 条异常考勤记录", "high", "danger", "/attendance");
        add(todos, overduePlans, "overdue-plan", "plan", "逾期计划",
                "有 %d 项计划超过截止日期", "medium", "warning", "/plans");
        add(todos, upcomingMeetings, "upcoming-meeting", "meeting", "近期会议",
                "未来安排了 %d 场会议", "low", "info", "/meeting-calendar");
        return sort(todos);
    }

    public static List<DashboardTodoVO> personalTodos(long pendingLeave, long pendingOut,
                                                        long incompleteAttendance, long overduePlans,
                                                        long upcomingMeetings) {
        List<DashboardTodoVO> todos = new ArrayList<>();
        add(todos, incompleteAttendance, "incomplete-attendance", "attendance", "今日上下班打卡尚未完整",
                "请确认今天的上班和下班打卡记录", "high", "danger", "/attendance");
        add(todos, pendingLeave, "pending-leave", "approval", "我的请假审批",
                "有 %d 项请假申请等待结果", "high", "warning", "/approvals");
        add(todos, pendingOut, "pending-out", "approval", "我的外出审批",
                "有 %d 项外出申请等待结果", "high", "warning", "/approvals");
        add(todos, overduePlans, "overdue-plan", "plan", "逾期个人计划",
                "有 %d 项个人计划超过截止日期", "medium", "warning", "/plans");
        add(todos, upcomingMeetings, "upcoming-meeting", "meeting", "近期会议",
                "未来安排了 %d 场会议", "low", "info", "/meeting-calendar");
        return sort(todos);
    }

    private static void add(List<DashboardTodoVO> todos, long count, String key, String category,
                            String title, String descriptionTemplate, String priority,
                            String semantic, String targetPath) {
        if (count <= 0) {
            return;
        }
        String description = descriptionTemplate.contains("%d")
                ? String.format(descriptionTemplate, count) : descriptionTemplate;
        todos.add(new DashboardTodoVO(key, category, title, description, count, priority, semantic, targetPath));
    }

    private static List<DashboardTodoVO> sort(List<DashboardTodoVO> todos) {
        return todos.stream()
                .sorted(Comparator.comparingInt((DashboardTodoVO item) -> priorityRank(item.getPriority())))
                .toList();
    }

    private static int priorityRank(String priority) {
        return switch (priority) {
            case "high" -> 0;
            case "medium" -> 1;
            default -> 2;
        };
    }
}
