package com.company.enterprise.service;

import com.company.enterprise.common.TokenUser;
import com.company.enterprise.common.UserContext;
import com.company.enterprise.common.PageResult;
import com.company.enterprise.dto.MessageQuery;
import com.company.enterprise.entity.Message;
import com.company.enterprise.mapper.MessageMapper;
import com.company.enterprise.mapper.NoticeMapper;
import com.company.enterprise.mapper.UserMapper;
import com.company.enterprise.service.impl.MessageServiceImpl;
import com.company.enterprise.vo.MessageSummaryVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private NoticeMapper noticeMapper;

    @Mock
    private UserMapper userMapper;

    private MessageServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MessageServiceImpl(messageMapper, noticeMapper, userMapper);
        UserContext.set(new TokenUser(7L, "alice", "Alice", 3L, "RND_ENGINEER", 11L));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void pageAlwaysUsesCurrentUserWhenLoadingMessages() {
        MessageQuery query = new MessageQuery();
        Message message = new Message();
        message.setId(9L);
        message.setPriority(2);
        message.setCreateTime(LocalDateTime.of(2026, 9, 16, 10, 0));
        when(messageMapper.countPage(query, 7L)).thenReturn(1L);
        when(messageMapper.selectPage(query, 7L, 0L, 12L)).thenReturn(List.of(message));

        PageResult<Message> result = service.page(query);

        assertEquals(1L, result.getTotal());
        assertEquals(List.of(9L), result.getRecords().stream().map(Message::getId).toList());
        verify(messageMapper).countPage(query, 7L);
        verify(messageMapper).selectPage(query, 7L, 0L, 12L);
    }

    @Test
    void recentReadsCurrentUserMessagesWithoutSynchronizingNotices() {
        Message message = new Message();
        message.setId(10L);
        message.setPriority(2);
        message.setCreateTime(LocalDateTime.of(2026, 9, 16, 10, 0));
        when(messageMapper.selectPage(any(MessageQuery.class), eq(7L), eq(0L), eq(8L)))
                .thenReturn(List.of(message));

        List<Message> result = service.recent(8);

        assertEquals(List.of(10L), result.stream().map(Message::getId).toList());
        verify(messageMapper).selectPage(any(MessageQuery.class), eq(7L), eq(0L), eq(8L));
    }

    @Test
    void markReadDelegatesWithCurrentUserScope() {
        service.markRead(18L);

        verify(messageMapper).markRead(18L, 7L);
    }

    @Test
    void summaryReportsUnreadAndAllHighPriorityMessages() {
        when(messageMapper.countUnread(7L)).thenReturn(2L);
        when(messageMapper.countHighPriorityUnread(7L)).thenReturn(3L);
        when(messageMapper.countByType(eq(7L), any(String.class))).thenReturn(1L);

        MessageSummaryVO summary = service.summary();

        assertEquals(2L, summary.getUnread());
        assertEquals(3L, summary.getHighPriority());
        assertEquals(6L, summary.getTotal());
    }
}
