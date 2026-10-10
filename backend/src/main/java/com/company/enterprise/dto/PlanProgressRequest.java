package com.company.enterprise.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlanProgressRequest {

    @NotNull(message = "不能为空")
    private Long planId;

    @NotNull(message = "不能为空")
    @Min(value = 0, message = "不能小于0")
    @Max(value = 100, message = "不能大于100")
    private Integer progress;

    @NotBlank(message = "不能为空")
    private String content;
}
