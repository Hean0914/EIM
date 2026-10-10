package com.company.enterprise.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AiAssistantIntentRouterTest {

    @Test
    void routesOverduePlanQuestion() {
        assertEquals(AiAssistantIntentRouter.Intent.OVERDUE_PLANS,
                AiAssistantIntentRouter.route("哪些计划已经逾期？"));
    }

    @Test
    void routesWeeklyReportQuestion() {
        assertEquals(AiAssistantIntentRouter.Intent.WEEKLY_REPORT,
                AiAssistantIntentRouter.route("帮我生成本周工作周报"));
    }

    @Test
    void routesGeneralWorkbenchQuestion() {
        assertEquals(AiAssistantIntentRouter.Intent.WORKBENCH,
                AiAssistantIntentRouter.route("我今天有哪些待办？"));
    }

    @Test
    void routesWebSearchQuestion() {
        assertEquals(AiAssistantIntentRouter.Intent.WEB_SEARCH,
                AiAssistantIntentRouter.route("请联网搜索 Spring AI 最新资料"));
    }
}
