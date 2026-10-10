package com.company.enterprise.service.impl;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.dto.MeetingBookingQuery;
import com.company.enterprise.dto.MeetingBookingRequest;
import com.company.enterprise.dto.MeetingCalendarQuery;
import com.company.enterprise.common.UserContext;
import com.company.enterprise.entity.MeetingBooking;
import com.company.enterprise.entity.MeetingRoom;
import com.company.enterprise.mapper.MeetingBookingMapper;
import com.company.enterprise.mapper.MeetingRoomMapper;
import com.company.enterprise.service.MeetingBookingService;
import com.company.enterprise.service.MeetingCalendarAssembler;
import com.company.enterprise.vo.MeetingCalendarItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MeetingBookingServiceImpl implements MeetingBookingService {

    private final MeetingBookingMapper meetingBookingMapper;
    private final MeetingRoomMapper meetingRoomMapper;

    public MeetingBookingServiceImpl(MeetingBookingMapper meetingBookingMapper, MeetingRoomMapper meetingRoomMapper) {
        this.meetingBookingMapper = meetingBookingMapper;
        this.meetingRoomMapper = meetingRoomMapper;
    }

    @Override
    public PageResult<MeetingBooking> page(MeetingBookingQuery query) {
        long current = ServiceSupport.normalizeCurrent(query.getCurrent());
        long size = ServiceSupport.normalizeSize(query.getSize());
        long offset = (current - 1) * size;
        Long total = meetingBookingMapper.countBookingPage(query);
        List<MeetingBooking> records = total == null || total == 0
                ? List.of()
                : meetingBookingMapper.selectBookingPage(query, offset, size);
        return new PageResult<>(total == null ? 0 : total, current, size, records);
    }

    @Override
    public List<MeetingCalendarItem> calendar(MeetingCalendarQuery query) {
        if (query.getStartTime() == null || query.getEndTime() == null
                || !query.getEndTime().isAfter(query.getStartTime())) {
            throw new BusinessException("会议日历时间范围无效");
        }
        return MeetingCalendarAssembler.toItems(
                meetingBookingMapper.selectCalendar(query),
                UserContext.requireUserId(),
                UserContext.canManageOffice()
        );
    }

    @Override
    public MeetingBooking detail(Long id) {
        MeetingBooking booking = requireBooking(id);
        ServiceSupport.requireCurrentUserOrAdmin(booking.getBookerId(), "只能查看自己的会议室预定");
        return booking;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(MeetingBookingRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        MeetingRoom room = meetingRoomMapper.selectRoomById(request.getRoomId());
        if (room == null) {
            throw new BusinessException("会议室不存在");
        }
        if (room.getStatus() == null || room.getStatus() != 1) {
            throw new BusinessException("会议室不可用");
        }

        MeetingBooking booking = new MeetingBooking();
        booking.setBookingNo(ServiceSupport.businessNo("MB"));
        booking.setRoomId(request.getRoomId());
        booking.setBookerId(ServiceSupport.requireUserId());
        booking.setMeetingTitle(request.getMeetingTitle());
        booking.setParticipantCount(request.getParticipantCount() == null ? 0 : request.getParticipantCount());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setStatus(0);
        booking.setRemark(request.getRemark());
        if (meetingBookingMapper.countConflict(booking) > 0) {
            throw new BusinessException("该时间段会议室已被预定");
        }
        meetingBookingMapper.insertBooking(booking);
        return booking.getId();
    }

    @Override
    public void cancel(Long id) {
        MeetingBooking booking = requireBooking(id);
        ServiceSupport.requireCurrentUserOrAdmin(booking.getBookerId(), "只能取消自己的会议室预定");
        meetingBookingMapper.updateBookingStatus(id, 3);
    }

    @Override
    public void delete(Long id) {
        requireBooking(id);
        meetingBookingMapper.logicDeleteBookingById(id);
    }

    private MeetingBooking requireBooking(Long id) {
        if (id == null) {
            throw new BusinessException("预定ID不能为空");
        }
        MeetingBooking booking = meetingBookingMapper.selectBookingById(id);
        if (booking == null) {
            throw new BusinessException("会议室预定不存在");
        }
        return booking;
    }
}
