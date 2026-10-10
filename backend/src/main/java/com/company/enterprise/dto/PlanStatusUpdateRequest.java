package com.company.enterprise.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlanStatusUpdateRequest {

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "计划状态不能小于0")
    @Max(value = 4, message = "计划状态不能大于4")
    private Integer status;
}
