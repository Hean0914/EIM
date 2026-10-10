package com.company.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MeetingBookingRequest {

    @NotNull(message = "不能为空")
    private Long roomId;

    @NotBlank(message = "不能为空")
    private String meetingTitle;

    private Integer participantCount = 0;

    @NotNull(message = "不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "不能为空")
    private LocalDateTime endTime;

    private String remark;
}
