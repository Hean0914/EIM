package com.company.enterprise.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.company.enterprise.common.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("t_meeting_booking")
@EqualsAndHashCode(callSuper = true)
public class MeetingBooking extends BaseEntity {
    private String bookingNo;
    private Long roomId;
    private Long bookerId;
    private String meetingTitle;
    private Integer participantCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private String remark;

    @TableField(exist = false)
    private String roomName;

    @TableField(exist = false)
    private String location;

    @TableField(exist = false)
    private String equipment;

    @TableField(exist = false)
    private String bookerName;
}
