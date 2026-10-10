package com.company.enterprise.service;

import com.company.enterprise.entity.MeetingBooking;
import com.company.enterprise.vo.MeetingCalendarItem;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeetingCalendarAssemblerTest {

    @Test
    void masksOtherEmployeesDetailsButKeepsRoomOccupancyVisible() {
        MeetingBooking own = booking(1L, 10L, "我的会议");
        MeetingBooking other = booking(2L, 20L, "薪资评审");

        List<MeetingCalendarItem> items = MeetingCalendarAssembler.toItems(
                List.of(own, other), 10L, false
        );

        assertEquals("我的会议", items.get(0).getMeetingTitle());
        assertFalse(items.get(0).isMasked());
        assertEquals("已占用", items.get(1).getMeetingTitle());
        assertTrue(items.get(1).isMasked());
        assertNull(items.get(1).getBookerId());
        assertNull(items.get(1).getRemark());
        assertEquals("待开始", items.get(0).getStatusText());
    }

    @Test
    void keepsFullDetailsForOfficeManagers() {
        MeetingBooking booking = booking(7L, 99L, "季度复盘");
        booking.setStatus(1);

        MeetingCalendarItem item = MeetingCalendarAssembler.toItems(
                List.of(booking), 10L, true
        ).get(0);

        assertEquals("季度复盘", item.getMeetingTitle());
        assertEquals(99L, item.getBookerId());
        assertEquals("进行中", item.getStatusText());
        assertFalse(item.isMasked());
    }

    private MeetingBooking booking(Long id, Long bookerId, String title) {
        MeetingBooking booking = new MeetingBooking();
        booking.setId(id);
        booking.setBookingNo("MB-" + id);
        booking.setRoomId(1L);
        booking.setRoomName("第一会议室");
        booking.setLocation("办公楼3层301");
        booking.setBookerId(bookerId);
        booking.setBookerName(bookerId == 10L ? "当前员工" : "其他员工");
        booking.setMeetingTitle(title);
        booking.setParticipantCount(6);
        booking.setStartTime(LocalDateTime.of(2026, 9, 16, 10, 0));
        booking.setEndTime(LocalDateTime.of(2026, 9, 16, 11, 0));
        booking.setStatus(0);
        booking.setRemark("会议备注");
        return booking;
    }
}
