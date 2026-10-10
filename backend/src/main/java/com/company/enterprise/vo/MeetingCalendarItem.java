package com.company.enterprise.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MeetingCalendarItem {

    private Long id;
    private String bookingNo;
    private Long roomId;
    private String roomName;
    private String location;
    private String equipment;
    private Long bookerId;
    private String bookerName;
    private String meetingTitle;
    private Integer participantCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private String statusText;
    private String remark;
    private boolean masked;
}
