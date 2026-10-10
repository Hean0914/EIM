package com.company.enterprise.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressBookRequest {

    private Long id;
    private Long employeeId;

    @NotBlank(message = "不能为空")
    private String contactName;

    private String deptName;
    private String positionName;
    private String phone;

    @Email(message = "格式不正确")
    private String email;

    private String officePhone;
    private Integer sortOrder = 0;
    private Integer status = 1;
}
