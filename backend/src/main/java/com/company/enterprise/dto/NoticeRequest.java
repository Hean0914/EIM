package com.company.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoticeRequest {

    private Long id;

    @NotBlank(message = "不能为空")
    private String title;

    @NotBlank(message = "不能为空")
    private String content;

    private Integer noticeType = 1;
    private Integer publishStatus = 0;
    private Integer topFlag = 0;
}
