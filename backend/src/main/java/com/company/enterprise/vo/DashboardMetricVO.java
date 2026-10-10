package com.company.enterprise.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricVO {

    private String label;
    private Long value;
    private String unit;
    private String description;
    private String semantic;
}
