package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("t_role")
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseEntity {
    private String roleCode;
    private String roleName;
    private Integer status;
    private String remark;
}
