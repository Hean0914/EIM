package com.company.enterprise.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceAnomalyDistribution {

    private String type;
    private String label;
    private int count;
}
