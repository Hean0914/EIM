package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@TableName("t_employee")
@EqualsAndHashCode(callSuper = true)
public class Employee extends BaseEntity {
    private String employeeNo;
    private Long userId;
    private Long deptId;
    private Long positionId;
    private String employeeName;
    private Integer gender;
    private LocalDate birthday;
    private String idCard;
    private String phone;
    private String email;
    private LocalDate hireDate;
    private LocalDate regularDate;
    private LocalDate resignDate;
    private Integer employmentStatus;
    private String address;
    private String emergencyContact;
    private String emergencyPhone;
    private String remark;
}
