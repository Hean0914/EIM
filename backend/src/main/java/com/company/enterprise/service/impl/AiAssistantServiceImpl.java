package com.company.enterprise.service.impl;

import com.company.enterprise.common.TokenUser;
import com.company.enterprise.common.UserContext;
import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.dto.AiChatRequest;
import com.company.enterprise.dto.ApprovalQuery;
import com.company.enterprise.dto.MeetingCalendarQuery;
import com.company.enterprise.dto.PlanQuery;
import com.company.enterprise.service.AiAssistantContextAssembler;
import com.company.enterprise.service.AiAssistantInsightService;
import com.company.enterprise.service.AiAssistantIntentRouter;
import com.company.enterprise.service.AiAssistantService;
import com.company.enterprise.service.DashboardService;
import com.company.enterprise.service.LeaveApplicationService;
import com.company.enterprise.service.MeetingBookingService;
import com.company.enterprise.service.MessageService;
import com.company.enterprise.service.OutApplicationService;
import com.company.enterprise.service.PlanService;
import com.company.enterprise.service.WebSearchService;
import com.company.enterprise.entity.LeaveApplication;
import com.company.enterprise.entity.Message;
import com.company.enterprise.entity.OutApplication;
import com.company.enterprise.entity.Plan;
import com.company.enterprise.vo.AdminDashboardVO;
import com.company.enterprise.vo.AiChatResponse;
import com.company.enterprise.vo.MeetingCalendarItem;
import com.company.enterprise.vo.PersonalDashboardVO;
import com.company.enterprise.vo.WebSearchSourceVO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final String SYSTEM_PROMPT = """
            你是企业信息管理系统的本地办公助手。
            只允许依据“业务上下文”回答，不要编造不存在的数据。
            如果上下文没有答案，请明确说“当前业务上下文没有这项信息”，并建议用户打开对应模块。
            你不能审批、修改、删除任何数据，也不能执行 SQL。
            回答使用简洁、清晰的中文；涉及数量时保留具体数字。
            """;

    private final ChatClient chatClient;
    private final DashboardService dashboardService;
    private final MessageService messageService;
    private final PlanService planService;
    private final MeetingBookingService meetingBookingService;
    private final LeaveApplicationService leaveApplicationService;
    private final OutApplicationService outApplicationService;
    private final WebSearchService webSearchService;
    private final String model;

    public AiAssistantServiceImpl(ChatClient chatClient,
                                  DashboardService dashboardService,
                                  MessageService messageService,
                                  PlanService planService,
                                  MeetingBookingService meetingBookingService,
                                  LeaveApplicationService leaveApplicationService,
                                  OutApplicationService outApplicationService,
                                  WebSearchService webSearchService,
                                  @Value("${spring.ai.ollama.chat.options.model:qwen2.5:1.5b}") String model) {
        this.chatClient = chatClient;
        this.dashboardService = dashboardService;
        this.messageService = messageService;
        this.planService = planService;
        this.meetingBookingService = meetingBookingService;
        this.leaveApplicationService = leaveApplicationService;
        this.outApplicationService = outApplicationService;
        this.webSearchService = webSearchService;
        this.model = model;
    }

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        TokenUser user = UserContext.get();
        if (user == null) {
            throw new BusinessException(401, "请先登录");
        }
        AiAssistantIntentRouter.Intent intent = AiAssistantIntentRouter.route(request.getMessage());
        List<WebSearchSourceVO> sources = intent == AiAssistantIntentRouter.Intent.WEB_SEARCH
                ? webSearchService.search(request.getMessage())
                : List.of();
        String context = user.getRoleCode() != null && UserContext.canManagePeople()
                ? managementContext(user, intent)
                : personalContext(user, intent);
        try {
            String answer = chatClient.prompt()
                    .system(SYSTEM_PROMPT
                            + "\n当前问题类型：" + intentLabel(intent)
                            + "\n回答要求：" + answerInstruction(intent)
                            + "\n联网检索结果：\n" + formatSearchSources(sources)
                            + "\n业务上下文：\n" + context)
                    .user(prepareUserPrompt(request.getMessage()))
                    .call()
                    .content();
            if (answer == null || answer.isBlank()) {
                throw new BusinessException(503, "本地 AI 未返回有效回答，请稍后重试");
            }
            return new AiChatResponse(answer.trim(), model, !sources.isEmpty(), sources);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(503, "本地 AI 服务不可用，请确认 Ollama 已启动且模型可用");
        }
    }

    private String personalContext(TokenUser user, AiAssistantIntentRouter.Intent intent) {
        PersonalDashboardVO dashboard = dashboardService.personalDashboard();
        return buildContext(user, dashboard, null, intent);
    }

    private String managementContext(TokenUser user, AiAssistantIntentRouter.Intent intent) {
        AdminDashboardVO dashboard = UserContext.isGeneralManager()
                ? dashboardService.adminDashboard(7)
                : dashboardService.hrDashboard();
        return buildContext(user, null, dashboard, intent);
    }

    private String buildContext(TokenUser user,
                                PersonalDashboardVO personal,
                                AdminDashboardVO management,
                                AiAssistantIntentRouter.Intent intent) {
        boolean manager = UserContext.canManagePeople();
        List<Message> messages = messageService.recent(8);

        PlanQuery planQuery = new PlanQuery();
        planQuery.setCurrent(1L);
        planQuery.setSize(10L);
        if (!manager) {
            planQuery.setEmployeeId(user.getEmployeeId());
            planQuery.setIncludeCompanyPlan(true);
        }
        List<Plan> plans = planService.page(planQuery).getRecords();

        MeetingCalendarQuery meetingQuery = new MeetingCalendarQuery();
        meetingQuery.setStartTime(LocalDateTime.now());
        meetingQuery.setEndTime(LocalDateTime.now().plusDays(30));
        List<MeetingCalendarItem> meetings = meetingBookingService.calendar(meetingQuery);

        ApprovalQuery approvalQuery = new ApprovalQuery();
        approvalQuery.setCurrent(1L);
        approvalQuery.setSize(10L);
        if (!manager) {
            approvalQuery.setEmployeeId(user.getEmployeeId());
        }
        List<LeaveApplication> leaves = leaveApplicationService.page(approvalQuery).getRecords();
        List<OutApplication> outs = outApplicationService.page(approvalQuery).getRecords();

        String context = manager
                ? AiAssistantContextAssembler.management(user, management, messages, plans, meetings, leaves, outs)
                : AiAssistantContextAssembler.personal(user, personal, messages, plans, meetings, leaves, outs);
        AiAssistantInsightService.PlanInsight planInsight =
                AiAssistantInsightService.analyzePlans(plans, LocalDate.now());
        AiAssistantInsightService.ApprovalInsight approvalInsight =
                AiAssistantInsightService.analyzeApprovals(leaves, outs, LocalDateTime.now());
        return context
                + "\n确定性分析：\n"
                + "计划风险：逾期 " + planInsight.overdueCount() + " 项，临期 " + planInsight.nearDueCount() + " 项。\n"
                + planInsight.summary() + "\n"
                + "审批风险：待审批 " + approvalInsight.pendingCount() + " 项，停留超过 3 天 "
                + approvalInsight.agingCount() + " 项。\n"
                + approvalInsight.summary() + "\n";
    }

    public static String prepareUserPrompt(String message) {
        return "请使用清晰中文回答，最多 8 条短句或列表项。\n"
                + "输出至少包含“结论”，有风险时补充“风险”，最后给出“建议”。\n"
                + message.trim()
                + "\n/no_think";
    }

    private static String intentLabel(AiAssistantIntentRouter.Intent intent) {
        return switch (intent) {
            case WORKBENCH -> "工作台待办";
            case OVERDUE_PLANS -> "计划逾期与临期分析";
            case MEETINGS -> "会议日程";
            case APPROVALS -> "审批查询";
            case RISKS -> "综合风险分析";
            case DAILY_REPORT -> "日报生成";
            case WEEKLY_REPORT -> "周报生成";
            case ATTENDANCE -> "考勤分析";
            case MESSAGES -> "消息与公告";
            case WEB_SEARCH -> "联网资料检索";
            case GENERAL -> "综合问答";
        };
    }

    private static String answerInstruction(AiAssistantIntentRouter.Intent intent) {
        return switch (intent) {
            case DAILY_REPORT -> "按今日完成、待办、风险、明日建议四部分生成可复制日报。";
            case WEEKLY_REPORT -> "按本周完成、计划进度、审批与会议、风险、下周建议五部分生成可复制周报。";
            case OVERDUE_PLANS -> "优先列出逾期和两日内到期计划，标明截止日期、进度、风险等级和建议。";
            case RISKS -> "只依据确定性分析列出风险，按高、中、低排序，不要编造原因。";
            case APPROVALS -> "列出待审批数量、停留超过三天的数量和处理建议。";
            case WEB_SEARCH -> "仅基于联网检索结果回答，标注信息来源；没有可用来源时明确说明无法完成检索。";
            default -> "直接回答问题，引用上下文中的具体数量和日期；没有数据时明确说明。";
        };
    }

    private static String formatSearchSources(List<WebSearchSourceVO> sources) {
        if (sources == null || sources.isEmpty()) {
            return "未启用联网检索，或当前没有可用搜索结果。\n";
        }
        StringBuilder text = new StringBuilder();
        for (int index = 0; index < sources.size(); index++) {
            WebSearchSourceVO source = sources.get(index);
            text.append(index + 1).append(". ")
                    .append(source.getTitle()).append("\n")
                    .append("URL: ").append(source.getUrl()).append("\n")
                    .append("摘要: ").append(source.getSnippet()).append("\n");
        }
        return text.toString();
    }
}
