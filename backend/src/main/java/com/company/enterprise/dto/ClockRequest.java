package com.company.enterprise.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClockRequest {

    /** 高德 AMap.Geolocation 返回的 GCJ-02 经度。 */
    @NotNull(message = "无法获取GPS经度")
    @DecimalMin(value = "-180.0000000", message = "GPS经度不合法")
    @DecimalMax(value = "180.0000000", message = "GPS经度不合法")
    private BigDecimal longitude;

    /** 高德 AMap.Geolocation 返回的 GCJ-02 纬度。 */
    @NotNull(message = "无法获取GPS纬度")
    @DecimalMin(value = "-90.0000000", message = "GPS纬度不合法")
    @DecimalMax(value = "90.0000000", message = "GPS纬度不合法")
    private BigDecimal latitude;

    /** 高德定位返回的定位精度，单位：米。 */
    @NotNull(message = "无法获取GPS定位精度")
    @DecimalMin(value = "0.0", inclusive = false, message = "GPS定位精度不合法")
    private BigDecimal accuracy;

    private String address;
    private String remark;
}
