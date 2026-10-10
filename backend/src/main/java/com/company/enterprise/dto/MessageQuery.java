package com.company.enterprise.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class MessageQuery {

    @Min(value = 1, message = "必须大于等于1")
    private Long current = 1L;

    @Min(value = 1, message = "必须大于等于1")
    private Long size = 12L;

    private String messageType;
    private Integer readStatus;
    private Integer priority;
}
