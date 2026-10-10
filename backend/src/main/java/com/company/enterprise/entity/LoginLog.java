package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("t_login_log")
@EqualsAndHashCode(callSuper = true)
public class LoginLog extends BaseEntity {
    private Long userId;
    private String username;
    private String loginIp;
    private String userAgent;
    private Integer loginStatus;
    private String message;
}
