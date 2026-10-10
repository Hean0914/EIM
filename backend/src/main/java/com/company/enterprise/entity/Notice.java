package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("t_notice")
@EqualsAndHashCode(callSuper = true)
public class Notice extends BaseEntity {
    private String title;
    private String content;
    private Integer noticeType;
    private Integer publishStatus;
    private Long publisherId;
    private LocalDateTime publishTime;
    private Integer topFlag;

    @TableField(exist = false)
    private Boolean readFlag;
}
