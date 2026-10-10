package com.company.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentSaveRequest {

    private Long parentId = 0L;

    @NotBlank(message = "不能为空")
    private String deptCode;

    @NotBlank(message = "不能为空")
    private String deptName;

    private Long leaderId;
    private Integer sortOrder = 0;
    private Integer status = 1;
    private String remark;
}
