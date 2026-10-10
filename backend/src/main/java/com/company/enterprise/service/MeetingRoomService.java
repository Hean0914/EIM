package com.company.enterprise.service;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.dto.MeetingRoomQuery;
import com.company.enterprise.dto.MeetingRoomRequest;
import com.company.enterprise.entity.MeetingRoom;

import java.util.List;

public interface MeetingRoomService {

    PageResult<MeetingRoom> page(MeetingRoomQuery query);

    List<MeetingRoom> availableList();

    MeetingRoom detail(Long id);

    Long create(MeetingRoomRequest request);

    void update(MeetingRoomRequest request);

    void delete(Long id);
}
