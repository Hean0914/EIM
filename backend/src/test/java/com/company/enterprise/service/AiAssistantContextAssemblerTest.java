package com.company.enterprise.service;

import com.company.enterprise.common.TokenUser;
import com.company.enterprise.entity.LeaveApplication;
import com.company.enterprise.entity.Message;
import com.company.enterprise.entity.OutApplication;
import com.company.enterprise.entity.Plan;
import com.company.enterprise.vo.MeetingCalendarItem;
import com.company.enterprise.vo.DashboardMetricVO;
import com.company.enterprise.vo.DashboardTodoVO;
import com.company.enterprise.vo.PersonalDashboardVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiAssistantContextAssemblerTest {

    @Test
    void buildsPersonalContextFromAuthorizedDashboardData() {
        PersonalDashboardVO dashboard = new PersonalDashboardVO();
        dashboard.setMetrics(List.of(
                new DashboardMetricVO("待开始会议", 2L, "项", "本人待开始的会议预约", "info")
        ));
        dashboard.setTodos(List.of(
                new DashboardTodoVO(
                        "overdue-plan",
                        "计划",
                        "有 1 项个人计划已逾期",
                        "请打开计划管理查看",
                        1L,
                        "high",
                        "danger",
                        "/plans"
                )
        ));

        Message message = new Message();
        message.setTitle("计划提醒");
        message.setContent("请更新本周计划");
        Plan plan = new Plan();
        plan.setTitle("迭代开发");
        plan.setStartDate(java.time.LocalDate.parse("2026-09-20"));
        plan.setEndDate(java.time.LocalDate.parse("2026-09-30"));
        plan.setProgress(60);
        plan.setStatus(1);
        MeetingCalendarItem meeting = new MeetingCalendarItem();
        meeting.setMeetingTitle("周会");
        meeting.setRoomName("一号会议室");
        meeting.setStartTime(java.time.LocalDateTime.parse("2026-09-26T10:00:00"));
        meeting.setEndTime(java.time.LocalDateTime.parse("2026-09-26T11:00:00"));
        meeting.setStatusText("待开始");
        meeting.setMasked(true);
        LeaveApplication leave = new LeaveApplication();
        leave.setStatus(0);
        leave.setLeaveDays(new java.math.BigDecimal("1.5"));
        OutApplication out = new OutApplication();
        out.setStatus(0);

        String context = AiAssistantContextAssembler.personal(
                new TokenUser(7L, "alice", "张三", 3L, "RND_ENGINEER", 12L, null),
                dashboard,
                List.of(message),
                List.of(plan),
                List.of(meeting),
                List.of(leave),
                List.of(out)
        );

        assertTrue(context.contains("当前用户：张三"));
        assertTrue(context.contains("有 1 项个人计划已逾期"));
        assertTrue(context.contains("待开始会议：2项"));
        assertTrue(context.contains("请更新本周计划"));
        assertTrue(context.contains("迭代开发"));
        assertTrue(context.contains("周会"));
        assertTrue(context.contains("请假申请：待审批"));
        assertTrue(context.contains("外出申请：待审批"));
        assertFalse(context.contains("employeeId"));
        assertFalse(context.contains("password"));
        assertFalse(context.contains("token"));
    }

    @Test
    void masksMeetingDetailsWhenTheMeetingWasAlreadyRedacted() {
        MeetingCalendarItem meeting = new MeetingCalendarItem();
        meeting.setMeetingTitle("已占用");
        meeting.setRoomName("一号会议室");
        meeting.setMasked(true);

        String context = AiAssistantContextAssembler.personal(
                new TokenUser(7L, "alice", "张三", 3L, "RND_ENGINEER", 12L, null),
                null,
                List.<Message>of(),
                List.<Plan>of(),
                List.of(meeting),
                List.<LeaveApplication>of(),
                List.<OutApplication>of()
        );

        assertTrue(context.contains("已占用"));
        assertFalse(context.contains("某部门机密会议"));
    }
}
