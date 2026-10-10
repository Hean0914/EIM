package com.company.enterprise.vo;

import lombok.Data;

@Data
public class EmployeeAccountRoleVO {

    private Long employeeId;
    private String employeeNo;
    private String employeeName;
    private Long positionId;
    private String positionName;
    private Boolean accountAssigned;
    private Long userId;
    private String username;
    private Integer accountStatus;
    private Long roleId;
    private String roleCode;
    private String roleName;
}
