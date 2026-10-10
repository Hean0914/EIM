package com.company.enterprise.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    @NotBlank(message = "不能为空")
    private String realName;

    private String avatar;
    private String phone;

    @Email(message = "格式不正确")
    private String email;
}
