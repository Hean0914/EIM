package com.company.enterprise.vo;

import lombok.Data;

@Data
public class PositionRoleBindingVO {

    private Long positionId;
    private String positionCode;
    private String positionName;
    private Long deptId;
    private Long roleId;
    private String roleCode;
    private String roleName;
}
