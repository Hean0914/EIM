package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.dto.MeetingRoomQuery;
import com.company.enterprise.entity.MeetingRoom;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MeetingRoomMapper extends BaseMapper<MeetingRoom> {

    List<MeetingRoom> selectRoomPage(@Param("query") MeetingRoomQuery query,
                                     @Param("offset") Long offset,
                                     @Param("size") Long size);

    Long countRoomPage(@Param("query") MeetingRoomQuery query);

    List<MeetingRoom> selectAvailableRooms();

    MeetingRoom selectRoomById(@Param("id") Long id);

    Long countByRoomCode(@Param("roomCode") String roomCode, @Param("excludeId") Long excludeId);

    int insertRoom(MeetingRoom meetingRoom);

    int updateRoom(MeetingRoom meetingRoom);

    int logicDeleteRoomById(@Param("id") Long id);
}
