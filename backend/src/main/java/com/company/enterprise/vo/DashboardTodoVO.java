package com.company.enterprise.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTodoVO {

    private String key;
    private String category;
    private String title;
    private String description;
    private Long count;
    private String priority;
    private String semantic;
    private String targetPath;
}
