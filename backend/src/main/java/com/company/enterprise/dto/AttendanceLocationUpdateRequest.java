package com.company.enterprise.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AttendanceLocationUpdateRequest {

    @NotBlank(message = "打卡地点名称不能为空")
    private String locationName;

    private String address;

    @NotNull(message = "经度不能为空")
    @DecimalMin(value = "-180.0000000", message = "经度不能小于-180")
    @DecimalMax(value = "180.0000000", message = "经度不能大于180")
    private BigDecimal longitude;

    @NotNull(message = "纬度不能为空")
    @DecimalMin(value = "-90.0000000", message = "纬度不能小于-90")
    @DecimalMax(value = "90.0000000", message = "纬度不能大于90")
    private BigDecimal latitude;

    @NotNull(message = "允许打卡半径不能为空")
    @Min(value = 10, message = "允许打卡半径不能小于10米")
    @Max(value = 50000, message = "允许打卡半径不能大于50000米")
    private Integer allowedRadius;

    @NotNull(message = "定位精度上限不能为空")
    @Min(value = 5, message = "定位精度上限不能小于5米")
    @Max(value = 5000, message = "定位精度上限不能大于5000米")
    private Integer accuracyLimit;

    private String remark;
}
