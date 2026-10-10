package com.company.enterprise.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DepartmentTreeVO {

    private Long id;
    private Long parentId;
    private String deptCode;
    private String deptName;
    private Long leaderId;
    private Integer sortOrder;
    private Integer status;
    private String remark;
    private List<DepartmentTreeVO> children = new ArrayList<>();
}
