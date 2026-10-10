package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("t_department")
@EqualsAndHashCode(callSuper = true)
public class Department extends BaseEntity {
    private Long parentId;
    private String deptCode;
    private String deptName;
    private Long leaderId;
    private Integer sortOrder;
    private Integer status;
    private String remark;
}
