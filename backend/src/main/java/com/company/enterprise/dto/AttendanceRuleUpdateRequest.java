package com.company.enterprise.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class AttendanceRuleUpdateRequest {

    @NotNull(message = "上班打卡时间不能为空")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime workStartTime;

    @NotNull(message = "下班打卡时间不能为空")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime workEndTime;

    private String remark;
}
