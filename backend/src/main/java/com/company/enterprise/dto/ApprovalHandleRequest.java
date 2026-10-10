package com.company.enterprise.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApprovalHandleRequest {

    @NotNull(message = "不能为空")
    private Integer result;

    private String comment;
}
