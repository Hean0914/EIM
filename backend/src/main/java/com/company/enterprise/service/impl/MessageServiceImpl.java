package com.company.enterprise.service.impl;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.UserContext;
import com.company.enterprise.dto.MessageQuery;
import com.company.enterprise.entity.Message;
import com.company.enterprise.entity.Notice;
import com.company.enterprise.mapper.MessageMapper;
import com.company.enterprise.mapper.NoticeMapper;
import com.company.enterprise.mapper.UserMapper;
import com.company.enterprise.service.MessageAssembler;
import com.company.enterprise.service.MessageService;
import com.company.enterprise.vo.MessageSummaryVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final NoticeMapper noticeMapper;
    private final UserMapper userMapper;

    public MessageServiceImpl(MessageMapper messageMapper, NoticeMapper noticeMapper, UserMapper userMapper) {
        this.messageMapper = messageMapper;
        this.noticeMapper = noticeMapper;
        this.userMapper = userMapper;
    }

    @Override
    public PageResult<Message> page(MessageQuery query) {
        synchronizePublishedNotices();
        long current = ServiceSupport.normalizeCurrent(query.getCurrent());
        long size = ServiceSupport.normalizeSize(query.getSize());
        long offset = (current - 1) * size;
        Long userId = ServiceSupport.requireUserId();
        Long total = messageMapper.countPage(query, userId);
        List<Message> records = total == null || total == 0
                ? List.of()
                : messageMapper.selectPage(query, userId, offset, size);
        return new PageResult<>(total == null ? 0 : total, current, size, MessageAssembler.order(records));
    }

    @Override
    public List<Message> recent(int size) {
        long normalizedSize = Math.max(1, Math.min(size, 20));
        MessageQuery query = new MessageQuery();
        query.setCurrent(1L);
        query.setSize(normalizedSize);
        List<Message> records = messageMapper.selectPage(
                query,
                ServiceSupport.requireUserId(),
                0L,
                normalizedSize
        );
        return MessageAssembler.order(records);
    }

    @Override
    public MessageSummaryVO summary() {
        synchronizePublishedNotices();
        Long userId = ServiceSupport.requireUserId();
        MessageSummaryVO summary = new MessageSummaryVO();
        summary.setUnread(valueOf(messageMapper.countUnread(userId)));
        summary.setHighPriority(valueOf(messageMapper.countHighPriorityUnread(userId)));
        summary.setNoticeCount(valueOf(messageMapper.countByType(userId, "NOTICE")));
        summary.setApprovalCount(valueOf(messageMapper.countByType(userId, "APPROVAL")));
        summary.setAttendanceCount(valueOf(messageMapper.countByType(userId, "ATTENDANCE")));
        summary.setPlanCount(valueOf(messageMapper.countByType(userId, "PLAN")));
        summary.setMeetingCount(valueOf(messageMapper.countByType(userId, "MEETING")));
        summary.setTotal(summary.getNoticeCount() + summary.getApprovalCount() + summary.getAttendanceCount()
                + summary.getPlanCount() + summary.getMeetingCount()
                + valueOf(messageMapper.countByType(userId, "SYSTEM")));
        return summary;
    }

    @Override
    public void markRead(Long id) {
        messageMapper.markRead(id, ServiceSupport.requireUserId());
    }

    @Override
    public void markAllRead() {
        messageMapper.markAllRead(ServiceSupport.requireUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishNoticeMessage(Long noticeId, String title, String content, String targetPath) {
        if (noticeId == null) return;
        List<Long> userIds = userMapper.selectActiveUserIds();
        if (userIds == null) return;
        for (Long userId : userIds) {
            Message message = new Message();
            message.setRecipientUserId(userId);
            message.setMessageType("NOTICE");
            message.setSourceKey(MessageAssembler.sourceKey("NOTICE", noticeId));
            message.setTitle(title == null ? "新公告" : title);
            message.setContent(limit(content, 500));
            message.setPriority(2);
            message.setTargetPath(targetPath == null ? "/notices" : targetPath);
            messageMapper.insertIfAbsent(message);
        }
    }

    @Override
    public void publishApprovalMessage(Long employeeId, String businessType, Long businessId,
                                       String title, String content, String targetPath, Integer priority) {
        if (employeeId == null || businessId == null) return;
        Long userId = userMapper.selectUserIdByEmployeeId(employeeId);
        if (userId == null) return;
        Message message = new Message();
        message.setRecipientUserId(userId);
        message.setMessageType("APPROVAL");
        message.setSourceKey(MessageAssembler.sourceKey("APPROVAL", businessType + ":" + businessId));
        message.setTitle(title);
        message.setContent(limit(content, 500));
        message.setPriority(priority == null ? 2 : Math.max(1, Math.min(3, priority)));
        message.setTargetPath(targetPath == null ? "/approvals" : targetPath);
        messageMapper.insertIfAbsent(message);
    }

    private void synchronizePublishedNotices() {
        List<Notice> notices = noticeMapper.selectPublishedNotices();
        if (notices == null) return;
        for (Notice notice : notices) {
            publishNoticeMessage(notice.getId(), notice.getTitle(), notice.getContent(), "/notices");
        }
    }

    private Long valueOf(Long value) {
        return value == null ? 0L : value;
    }

    private String limit(String value, int maxLength) {
        if (value == null) return "";
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
