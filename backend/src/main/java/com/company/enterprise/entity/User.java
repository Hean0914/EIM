package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("t_user")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    private String username;
    private String password;
    private Long roleId;
    private Long employeeId;
    private String realName;
    private String avatar;
    private String phone;
    private String email;
    private Integer status;
    private LocalDateTime lastLoginTime;
}
