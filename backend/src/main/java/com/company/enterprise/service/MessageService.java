package com.company.enterprise.service;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.dto.MessageQuery;
import com.company.enterprise.entity.Message;
import com.company.enterprise.vo.MessageSummaryVO;

import java.util.List;

public interface MessageService {

    PageResult<Message> page(MessageQuery query);

    List<Message> recent(int size);

    MessageSummaryVO summary();

    void markRead(Long id);

    void markAllRead();

    void publishNoticeMessage(Long noticeId, String title, String content, String targetPath);

    void publishApprovalMessage(Long employeeId, String businessType, Long businessId,
                                String title, String content, String targetPath, Integer priority);
}
