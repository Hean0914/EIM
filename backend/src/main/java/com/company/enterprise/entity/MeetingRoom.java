package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("t_meeting_room")
@EqualsAndHashCode(callSuper = true)
public class MeetingRoom extends BaseEntity {
    private String roomName;
    private String roomCode;
    private String location;
    private Integer capacity;
    private String equipment;
    private Integer status;
    private String remark;
}
