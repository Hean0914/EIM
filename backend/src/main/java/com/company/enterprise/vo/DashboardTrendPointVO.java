package com.company.enterprise.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTrendPointVO {

    private String date;
    private String label;
    private Long value;
}
