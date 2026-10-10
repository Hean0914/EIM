package com.company.enterprise.service.impl;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.dto.NoticeQuery;
import com.company.enterprise.dto.NoticeRequest;
import com.company.enterprise.entity.Notice;
import com.company.enterprise.entity.NoticeRead;
import com.company.enterprise.mapper.NoticeMapper;
import com.company.enterprise.mapper.NoticeReadMapper;
import com.company.enterprise.service.NoticeService;
import com.company.enterprise.service.MessageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;
    private final NoticeReadMapper noticeReadMapper;
    private final MessageService messageService;

    public NoticeServiceImpl(NoticeMapper noticeMapper, NoticeReadMapper noticeReadMapper) {
        this(noticeMapper, noticeReadMapper, null);
    }

    @Autowired
    public NoticeServiceImpl(NoticeMapper noticeMapper, NoticeReadMapper noticeReadMapper,
                             MessageService messageService) {
        this.noticeMapper = noticeMapper;
        this.noticeReadMapper = noticeReadMapper;
        this.messageService = messageService;
    }

    @Override
    public PageResult<Notice> page(NoticeQuery query) {
        long current = ServiceSupport.normalizeCurrent(query.getCurrent());
        long size = ServiceSupport.normalizeSize(query.getSize());
        long offset = (current - 1) * size;
        Long total = noticeMapper.countNoticePage(query);
        List<Notice> records = total == null || total == 0
                ? List.of()
                : noticeMapper.selectNoticePage(query, offset, size);
        applyReadFlags(records);
        return new PageResult<>(total == null ? 0 : total, current, size, records);
    }

    @Override
    public Notice detail(Long id) {
        Notice notice = requireNotice(id);
        requirePublishedForEmployee(notice);
        applyReadFlags(List.of(notice));
        return notice;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(NoticeRequest request) {
        Notice notice = new Notice();
        BeanUtils.copyProperties(request, notice);
        normalize(notice);
        notice.setPublisherId(ServiceSupport.requireUserId());
        if (notice.getPublishStatus() == 1) {
            notice.setPublishTime(LocalDateTime.now());
        }
        noticeMapper.insertNotice(notice);
        if (notice.getPublishStatus() == 1) {
            publishNoticeMessage(notice);
        }
        return notice.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(NoticeRequest request) {
        if (request.getId() == null) {
            throw new BusinessException("公告ID不能为空");
        }
        requireNotice(request.getId());
        Notice notice = new Notice();
        BeanUtils.copyProperties(request, notice);
        normalize(notice);
        notice.setPublisherId(ServiceSupport.requireUserId());
        if (notice.getPublishStatus() == 1) {
            notice.setPublishTime(LocalDateTime.now());
        }
        noticeMapper.updateNotice(notice);
        if (notice.getPublishStatus() == 1) {
            publishNoticeMessage(notice);
        }
    }

    @Override
    public void publish(Long id) {
        Notice notice = requireNotice(id);
        noticeMapper.updatePublishStatus(id, 1, ServiceSupport.requireUserId());
        publishNoticeMessage(notice);
    }

    @Override
    public void offline(Long id) {
        requireNotice(id);
        noticeMapper.updatePublishStatus(id, 2, ServiceSupport.requireUserId());
    }

    @Override
    public void markRead(Long id) {
        Notice notice = requireNotice(id);
        requirePublishedForEmployee(notice);
        Long userId = ServiceSupport.requireUserId();
        if (noticeReadMapper.countByNoticeAndUser(id, userId) > 0) {
            return;
        }
        NoticeRead noticeRead = new NoticeRead();
        noticeRead.setNoticeId(id);
        noticeRead.setUserId(userId);
        noticeRead.setReadTime(LocalDateTime.now());
        noticeReadMapper.insertNoticeRead(noticeRead);
    }

    @Override
    public void delete(Long id) {
        requireNotice(id);
        noticeMapper.logicDeleteNoticeById(id);
    }

    private Notice requireNotice(Long id) {
        if (id == null) {
            throw new BusinessException("公告ID不能为空");
        }
        Notice notice = noticeMapper.selectNoticeById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        return notice;
    }

    private void requirePublishedForEmployee(Notice notice) {
        if (!ServiceSupport.canManageOffice() && (notice.getPublishStatus() == null || notice.getPublishStatus() != 1)) {
            throw new BusinessException(403, "只能查看已发布公告");
        }
    }

    private void normalize(Notice notice) {
        if (notice.getNoticeType() == null) {
            notice.setNoticeType(1);
        }
        if (notice.getPublishStatus() == null) {
            notice.setPublishStatus(0);
        }
        if (notice.getTopFlag() == null) {
            notice.setTopFlag(0);
        }
    }

    private void publishNoticeMessage(Notice notice) {
        if (messageService != null) {
            messageService.publishNoticeMessage(notice.getId(), notice.getTitle(), notice.getContent(), "/notices");
        }
    }

    private void applyReadFlags(List<Notice> notices) {
        if (notices == null || notices.isEmpty()) {
            return;
        }
        List<Long> noticeIds = notices.stream()
                .map(Notice::getId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (noticeIds.isEmpty()) {
            return;
        }
        Set<Long> readIds = noticeReadMapper.selectReadNoticeIds(noticeIds, ServiceSupport.requireUserId());
        Set<Long> safeReadIds = readIds == null ? new HashSet<>() : readIds;
        notices.forEach(notice -> notice.setReadFlag(safeReadIds.contains(notice.getId())));
    }
}
