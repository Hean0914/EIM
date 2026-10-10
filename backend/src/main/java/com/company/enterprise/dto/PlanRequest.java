package com.company.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PlanRequest {

    private Long id;

    @NotNull(message = "不能为空")
    private Integer planType;

    @NotBlank(message = "不能为空")
    private String title;

    @NotBlank(message = "不能为空")
    private String content;

    private Long deptId;
    private Long employeeId;

    @NotNull(message = "不能为空")
    private LocalDate startDate;

    @NotNull(message = "不能为空")
    private LocalDate endDate;

    private Integer priority = 2;
    private Integer progress = 0;
    private Integer status = 0;
}
