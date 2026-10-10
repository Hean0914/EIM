package com.company.enterprise.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MeetingBookingQuery {

    @Min(value = 1, message = "必须大于等于1")
    private Long current = 1L;

    @Min(value = 1, message = "必须大于等于1")
    private Long size = 10L;

    private Long roomId;
    private Long bookerId;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
