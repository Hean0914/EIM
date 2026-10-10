package com.company.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiChatRequest {

    @NotBlank(message = "问题不能为空")
    @Size(max = 2000, message = "问题不能超过2000个字符")
    private String message;
}
