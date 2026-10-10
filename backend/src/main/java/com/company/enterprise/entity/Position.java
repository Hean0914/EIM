package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("t_position")
@EqualsAndHashCode(callSuper = true)
public class Position extends BaseEntity {
    private Long deptId;
    private String positionCode;
    private String positionName;
    private String positionLevel;
    private Integer sortOrder;
    private Integer status;
    private String remark;
}
