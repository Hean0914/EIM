package com.company.enterprise.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeSaveRequest {

    @NotBlank(message = "不能为空")
    private String employeeNo;

    @NotNull(message = "不能为空")
    private Long deptId;

    private Long positionId;

    @NotBlank(message = "不能为空")
    private String employeeName;

    private Integer gender = 0;
    private LocalDate birthday;
    private String idCard;
    private String phone;

    @Email(message = "格式不正确")
    private String email;

    private LocalDate hireDate;
    private LocalDate regularDate;
    private LocalDate resignDate;
    private Integer employmentStatus = 1;
    private String address;
    private String emergencyContact;
    private String emergencyPhone;
    private String remark;

    private String username;
    private String password;
    private Long roleId = 2L;
}
