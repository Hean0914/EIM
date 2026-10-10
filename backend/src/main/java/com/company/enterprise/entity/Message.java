package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("t_message")
@EqualsAndHashCode(callSuper = true)
public class Message extends BaseEntity {
    private Long recipientUserId;
    private String messageType;
    private String sourceKey;
    private String title;
    private String content;
    private Integer priority;
    private String targetPath;
    private Integer readStatus;
    private LocalDateTime readTime;
}
