package com.company.enterprise.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PlanQuery {

    @Min(value = 1, message = "必须大于等于1")
    private Long current = 1L;

    @Min(value = 1, message = "必须大于等于1")
    private Long size = 10L;

    private Integer planType;
    private String title;
    private Long deptId;
    private Long employeeId;
    private Boolean includeCompanyPlan;
    private Integer status;
}
