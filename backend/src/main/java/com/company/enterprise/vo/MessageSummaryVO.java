package com.company.enterprise.vo;

import lombok.Data;

@Data
public class MessageSummaryVO {
    private Long total = 0L;
    private Long unread = 0L;
    private Long highPriority = 0L;
    private Long noticeCount = 0L;
    private Long approvalCount = 0L;
    private Long attendanceCount = 0L;
    private Long planCount = 0L;
    private Long meetingCount = 0L;
}
