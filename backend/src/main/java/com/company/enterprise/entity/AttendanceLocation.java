package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("t_attendance_location")
@EqualsAndHashCode(callSuper = true)
public class AttendanceLocation extends BaseEntity {

    private String locationName;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer allowedRadius;
    private Integer accuracyLimit;
    private Integer status;
    private String remark;
}
