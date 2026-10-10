package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("t_notice_read")
@EqualsAndHashCode(callSuper = true)
public class NoticeRead extends BaseEntity {
    private Long noticeId;
    private Long userId;
    private LocalDateTime readTime;
}
