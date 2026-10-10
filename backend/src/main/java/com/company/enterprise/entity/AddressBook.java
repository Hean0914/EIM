package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("t_address_book")
@EqualsAndHashCode(callSuper = true)
public class AddressBook extends BaseEntity {
    private Long employeeId;
    private String contactName;
    private String deptName;
    private String positionName;
    private String phone;
    private String email;
    private String officePhone;
    private Integer sortOrder;
    private Integer status;
}
