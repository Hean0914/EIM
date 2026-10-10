package com.company.enterprise.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MeetingRoomRequest {

    private Long id;

    @NotBlank(message = "不能为空")
    private String roomName;

    @NotBlank(message = "不能为空")
    private String roomCode;

    @NotBlank(message = "不能为空")
    private String location;

    private Integer capacity = 0;
    private String equipment;
    private Integer status = 1;
    private String remark;
}
