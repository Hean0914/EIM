package com.company.enterprise.service;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.TokenUser;
import com.company.enterprise.common.UserContext;
import com.company.enterprise.dto.NoticeQuery;
import com.company.enterprise.entity.Notice;
import com.company.enterprise.mapper.NoticeMapper;
import com.company.enterprise.mapper.NoticeReadMapper;
import com.company.enterprise.service.impl.NoticeServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeServiceReadStateTest {

    @Mock
    private NoticeMapper noticeMapper;

    @Mock
    private NoticeReadMapper noticeReadMapper;

    private NoticeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NoticeServiceImpl(noticeMapper, noticeReadMapper);
        UserContext.set(new TokenUser(7L, "manager", "Manager", 1L, "GENERAL_MANAGER", 1L));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void pageMarksNoticesReadForCurrentUserWithoutChangingPersistedShape() {
        Notice unread = notice(10L);
        Notice read = notice(11L);
        NoticeQuery query = new NoticeQuery();
        when(noticeMapper.countNoticePage(query)).thenReturn(2L);
        when(noticeMapper.selectNoticePage(query, 0L, 10L)).thenReturn(List.of(unread, read));
        when(noticeReadMapper.selectReadNoticeIds(List.of(10L, 11L), 7L)).thenReturn(Set.of(11L));

        PageResult<Notice> result = service.page(query);

        assertFalse(result.getRecords().get(0).getReadFlag());
        assertTrue(result.getRecords().get(1).getReadFlag());
    }

    private Notice notice(Long id) {
        Notice notice = new Notice();
        notice.setId(id);
        notice.setPublishStatus(1);
        return notice;
    }
}
