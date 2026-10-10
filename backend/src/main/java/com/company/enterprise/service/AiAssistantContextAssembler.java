package com.company.enterprise.service;

import com.company.enterprise.common.TokenUser;
import com.company.enterprise.entity.LeaveApplication;
import com.company.enterprise.entity.Message;
import com.company.enterprise.entity.OutApplication;
import com.company.enterprise.entity.Plan;
import com.company.enterprise.vo.AdminDashboardVO;
import com.company.enterprise.vo.DashboardMetricVO;
import com.company.enterprise.vo.DashboardTodoVO;
import com.company.enterprise.vo.MeetingCalendarItem;
import com.company.enterprise.vo.PersonalDashboardVO;

import java.time.LocalDateTime;
import java.util.List;

public final class AiAssistantContextAssembler {

    private AiAssistantContextAssembler() {
    }

    public static String personal(TokenUser user, PersonalDashboardVO dashboard) {
        return personal(user, dashboard, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    public static String personal(TokenUser user,
                                  PersonalDashboardVO dashboard,
                                  List<Message> messages,
                                  List<Plan> plans,
                                  List<MeetingCalendarItem> meetings,
                                  List<LeaveApplication> leaves,
                                  List<OutApplication> outs) {
        StringBuilder context = new StringBuilder();
        appendUser(context, user);
        appendMetrics(context, dashboard == null ? List.of() : dashboard.getMetrics());
        appendTodos(context, dashboard == null ? List.of() : dashboard.getTodos());
        appendMessages(context, messages);
        appendPlans(context, plans);
        appendMeetings(context, meetings);
        appendApplications(context, leaves, outs);
        return context.toString();
    }

    public static String management(TokenUser user, AdminDashboardVO dashboard) {
        return management(user, dashboard, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    public static String management(TokenUser user,
                                    AdminDashboardVO dashboard,
                                    List<Message> messages,
                                    List<Plan> plans,
                                    List<MeetingCalendarItem> meetings,
                                    List<LeaveApplication> leaves,
                                    List<OutApplication> outs) {
        StringBuilder context = new StringBuilder();
        appendUser(context, user);
        appendMetrics(context, dashboard == null ? List.of() : dashboard.getMetrics());
        appendTodos(context, dashboard == null ? List.of() : dashboard.getTodos());
        appendMessages(context, messages);
        appendPlans(context, plans);
        appendMeetings(context, meetings);
        appendApplications(context, leaves, outs);
        return context.toString();
    }

    private static void appendUser(StringBuilder context, TokenUser user) {
        context.append("当前用户：")
                .append(value(user == null ? null : user.getRealName(), "用户"))
                .append("，岗位：")
                .append(value(user == null ? null : user.getRoleCode(), "未知"))
                .append('\n');
    }

    private static void appendMetrics(StringBuilder context, List<DashboardMetricVO> metrics) {
        context.append("工作台指标：\n");
        if (metrics == null || metrics.isEmpty()) {
            context.append("暂无指标\n");
            return;
        }
        for (DashboardMetricVO metric : metrics) {
            if (metric == null) {
                continue;
            }
            context.append("- ")
                    .append(value(metric.getLabel(), "指标"))
                    .append("：")
                    .append(metric.getValue() == null ? 0 : metric.getValue())
                    .append(value(metric.getUnit(), ""))
                    .append('\n');
        }
    }

    private static void appendTodos(StringBuilder context, List<DashboardTodoVO> todos) {
        context.append("工作台待办：\n");
        if (todos == null || todos.isEmpty()) {
            context.append("暂无待办\n");
            return;
        }
        for (DashboardTodoVO todo : todos) {
            if (todo == null) {
                continue;
            }
            context.append("- ")
                    .append(value(todo.getTitle(), "待办"))
                    .append("：")
                    .append(value(todo.getDescription(), "请查看对应业务模块"))
                    .append('\n');
        }
    }

    private static void appendMessages(StringBuilder context, List<Message> messages) {
        context.append("近期消息：\n");
        if (messages == null || messages.isEmpty()) {
            context.append("暂无近期消息\n");
            return;
        }
        for (Message message : messages) {
            if (message == null) {
                continue;
            }
            context.append("- ")
                    .append(value(message.getTitle(), "系统消息"))
                    .append("：")
                    .append(value(message.getContent(), "无摘要"))
                    .append('\n');
        }
    }

    private static void appendPlans(StringBuilder context, List<Plan> plans) {
        context.append("计划明细：\n");
        if (plans == null || plans.isEmpty()) {
            context.append("暂无计划\n");
            return;
        }
        for (Plan plan : plans) {
            if (plan == null) {
                continue;
            }
            context.append("- ")
                    .append(value(plan.getTitle(), "未命名计划"))
                    .append("（")
                    .append(value(plan.getStartDate() == null ? null : plan.getStartDate().toString(), "未设置开始日期"))
                    .append(" 至 ")
                    .append(value(plan.getEndDate() == null ? null : plan.getEndDate().toString(), "未设置结束日期"))
                    .append("，")
                    .append(planStatus(plan.getStatus()))
                    .append("，进度 ")
                    .append(plan.getProgress() == null ? 0 : plan.getProgress())
                    .append("%）\n");
        }
    }

    private static void appendMeetings(StringBuilder context, List<MeetingCalendarItem> meetings) {
        context.append("近期会议：\n");
        if (meetings == null || meetings.isEmpty()) {
            context.append("暂无近期会议\n");
            return;
        }
        for (MeetingCalendarItem meeting : meetings) {
            if (meeting == null) {
                continue;
            }
            context.append("- ")
                    .append(value(meeting.getMeetingTitle(), "已占用"))
                    .append("，会议室：")
                    .append(value(meeting.getRoomName(), "未设置"))
                    .append("，时间：")
                    .append(formatDateTime(meeting.getStartTime()))
                    .append(" 至 ")
                    .append(formatDateTime(meeting.getEndTime()))
                    .append("，状态：")
                    .append(value(meeting.getStatusText(), "未知"))
                    .append('\n');
        }
    }

    private static void appendApplications(StringBuilder context,
                                           List<LeaveApplication> leaves,
                                           List<OutApplication> outs) {
        context.append("审批申请：\n");
        boolean empty = (leaves == null || leaves.isEmpty()) && (outs == null || outs.isEmpty());
        if (empty) {
            context.append("暂无请假或外出申请\n");
            return;
        }
        if (leaves != null) {
            for (LeaveApplication leave : leaves) {
                if (leave != null) {
                    context.append("- 请假申请：").append(applicationStatus(leave.getStatus()))
                            .append("，时长 ").append(value(leave.getLeaveDays() == null ? null : leave.getLeaveDays().toString(), "未填写"))
                            .append(" 天\n");
                }
            }
        }
        if (outs != null) {
            for (OutApplication out : outs) {
                if (out != null) {
                    context.append("- 外出申请：").append(applicationStatus(out.getStatus())).append('\n');
                }
            }
        }
    }

    private static String planStatus(Integer status) {
        return switch (status == null ? -1 : status) {
            case 0 -> "未开始";
            case 1 -> "进行中";
            case 2 -> "已完成";
            case 3 -> "已延期";
            case 4 -> "已取消";
            default -> "未知状态";
        };
    }

    private static String applicationStatus(Integer status) {
        return switch (status == null ? -1 : status) {
            case 0 -> "待审批";
            case 1 -> "已通过";
            case 2 -> "已驳回";
            case 3 -> "已撤销";
            default -> "未知状态";
        };
    }

    private static String formatDateTime(LocalDateTime value) {
        return value == null ? "未设置" : value.toString().replace('T', ' ');
    }

    private static String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
