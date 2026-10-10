package com.company.enterprise.service;

import com.company.enterprise.entity.Message;
import com.company.enterprise.vo.MessageSummaryVO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageAssemblerTest {

    @Test
    void sortsHighPriorityMessagesBeforeNewestLowerPriorityMessages() {
        Message oldHigh = message(1L, 3, 10, "2026-09-15T09:00:00");
        Message newLow = message(2L, 1, 10, "2026-09-16T09:00:00");
        Message medium = message(3L, 2, 10, "2026-09-16T10:00:00");

        assertEquals(List.of(1L, 3L, 2L), MessageAssembler.order(List.of(newLow, medium, oldHigh))
                .stream().map(Message::getId).toList());
    }

    @Test
    void summarizesUnreadPriorityAndCategories() {
        Message notice = message(1L, 2, 0, "2026-09-16T09:00:00");
        notice.setMessageType("NOTICE");
        Message approval = message(2L, 3, 1, "2026-09-16T10:00:00");
        approval.setMessageType("APPROVAL");
        Message plan = message(3L, 1, 0, "2026-09-16T11:00:00");
        plan.setMessageType("PLAN");

        MessageSummaryVO summary = MessageAssembler.summarize(List.of(notice, approval, plan));

        assertEquals(3L, summary.getTotal());
        assertEquals(2L, summary.getUnread());
        assertEquals(1L, summary.getHighPriority());
        assertEquals(1L, summary.getNoticeCount());
        assertEquals(1L, summary.getApprovalCount());
        assertEquals(1L, summary.getPlanCount());
    }

    @Test
    void buildsStableSourceKeys() {
        assertEquals("NOTICE:12", MessageAssembler.sourceKey("NOTICE", 12L));
        assertEquals("TODO:OVERDUE_PLAN:2026-09-16", MessageAssembler.sourceKey("TODO", "OVERDUE_PLAN:2026-09-16"));
    }

    private Message message(Long id, Integer priority, Integer readStatus, String createdAt) {
        Message message = new Message();
        message.setId(id);
        message.setPriority(priority);
        message.setReadStatus(readStatus);
        message.setCreateTime(LocalDateTime.parse(createdAt));
        return message;
    }
}
