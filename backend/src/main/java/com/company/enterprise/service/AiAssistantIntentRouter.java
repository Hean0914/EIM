package com.company.enterprise.service;

import org.springframework.util.StringUtils;

import java.util.Locale;

public final class AiAssistantIntentRouter {

    public enum Intent {
        WORKBENCH,
        OVERDUE_PLANS,
        MEETINGS,
        APPROVALS,
        RISKS,
        DAILY_REPORT,
        WEEKLY_REPORT,
        ATTENDANCE,
        MESSAGES,
        WEB_SEARCH,
        GENERAL
    }

    private AiAssistantIntentRouter() {
    }

    public static Intent route(String message) {
        String text = StringUtils.hasText(message) ? message.trim().toLowerCase(Locale.ROOT) : "";
        if (containsAny(text, "联网", "上网", "搜索", "检索", "查找资料", "最新资料", "网络信息", "web")) {
            return Intent.WEB_SEARCH;
        }
        if (containsAny(text, "周报", "本周报告", "weekly")) {
            return Intent.WEEKLY_REPORT;
        }
        if (containsAny(text, "日报", "今天总结", "今日总结", "daily")) {
            return Intent.DAILY_REPORT;
        }
        if (containsAny(text, "逾期", "过期", "延期", "临期")) {
            return Intent.OVERDUE_PLANS;
        }
        if (containsAny(text, "风险", "滞留", "停留过久", "超时")) {
            return Intent.RISKS;
        }
        if (containsAny(text, "会议", "日程", "会议室")) {
            return Intent.MEETINGS;
        }
        if (containsAny(text, "审批", "请假", "外出申请")) {
            return Intent.APPROVALS;
        }
        if (containsAny(text, "考勤", "迟到", "早退", "缺勤")) {
            return Intent.ATTENDANCE;
        }
        if (containsAny(text, "消息", "公告", "通知")) {
            return Intent.MESSAGES;
        }
        if (containsAny(text, "待办", "工作台", "任务")) {
            return Intent.WORKBENCH;
        }
        return Intent.GENERAL;
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
