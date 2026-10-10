package com.company.enterprise.service.impl;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.dto.MeetingRoomQuery;
import com.company.enterprise.dto.MeetingRoomRequest;
import com.company.enterprise.entity.MeetingRoom;
import com.company.enterprise.mapper.MeetingRoomMapper;
import com.company.enterprise.service.MeetingRoomService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MeetingRoomServiceImpl implements MeetingRoomService {

    private final MeetingRoomMapper meetingRoomMapper;

    public MeetingRoomServiceImpl(MeetingRoomMapper meetingRoomMapper) {
        this.meetingRoomMapper = meetingRoomMapper;
    }

    @Override
    public PageResult<MeetingRoom> page(MeetingRoomQuery query) {
        long current = ServiceSupport.normalizeCurrent(query.getCurrent());
        long size = ServiceSupport.normalizeSize(query.getSize());
        long offset = (current - 1) * size;
        Long total = meetingRoomMapper.countRoomPage(query);
        List<MeetingRoom> records = total == null || total == 0
                ? List.of()
                : meetingRoomMapper.selectRoomPage(query, offset, size);
        return new PageResult<>(total == null ? 0 : total, current, size, records);
    }

    @Override
    public List<MeetingRoom> availableList() {
        return meetingRoomMapper.selectAvailableRooms();
    }

    @Override
    public MeetingRoom detail(Long id) {
        return requireRoom(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(MeetingRoomRequest request) {
        validateRoomCodeUnique(null, request.getRoomCode());
        MeetingRoom room = new MeetingRoom();
        BeanUtils.copyProperties(request, room);
        normalize(room);
        meetingRoomMapper.insertRoom(room);
        return room.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MeetingRoomRequest request) {
        if (request.getId() == null) {
            throw new BusinessException("会议室ID不能为空");
        }
        requireRoom(request.getId());
        validateRoomCodeUnique(request.getId(), request.getRoomCode());
        MeetingRoom room = new MeetingRoom();
        BeanUtils.copyProperties(request, room);
        normalize(room);
        meetingRoomMapper.updateRoom(room);
    }

    @Override
    public void delete(Long id) {
        requireRoom(id);
        meetingRoomMapper.logicDeleteRoomById(id);
    }

    private MeetingRoom requireRoom(Long id) {
        if (id == null) {
            throw new BusinessException("会议室ID不能为空");
        }
        MeetingRoom room = meetingRoomMapper.selectRoomById(id);
        if (room == null) {
            throw new BusinessException("会议室不存在");
        }
        return room;
    }

    private void validateRoomCodeUnique(Long id, String roomCode) {
        if (meetingRoomMapper.countByRoomCode(roomCode, id) > 0) {
            throw new BusinessException("会议室编码已存在");
        }
    }

    private void normalize(MeetingRoom room) {
        if (room.getCapacity() == null) {
            room.setCapacity(0);
        }
        if (room.getStatus() == null) {
            room.setStatus(1);
        }
    }
}
