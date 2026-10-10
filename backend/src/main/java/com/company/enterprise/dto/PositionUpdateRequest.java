package com.company.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PositionUpdateRequest {

    @NotNull(message = "不能为空")
    private Long id;

    private Long deptId;

    @NotBlank(message = "不能为空")
    private String positionCode;

    @NotBlank(message = "不能为空")
    private String positionName;

    private String positionLevel;
    private Integer sortOrder = 0;
    private Integer status = 1;
    private String remark;
}
