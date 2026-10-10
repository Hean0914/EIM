package com.company.enterprise.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class EmployeePageQuery {

    @Min(value = 1, message = "必须大于等于1")
    private Long current = 1L;

    @Min(value = 1, message = "必须大于等于1")
    private Long size = 10L;

    private String employeeNo;
    private String employeeName;
    private String phone;
    private Long deptId;
    private Long positionId;
    private Integer employmentStatus;
}
