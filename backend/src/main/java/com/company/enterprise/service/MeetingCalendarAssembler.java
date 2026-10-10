package com.company.enterprise.service;

import com.company.enterprise.entity.MeetingBooking;
import com.company.enterprise.vo.MeetingCalendarItem;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class MeetingCalendarAssembler {

    private static final String MASKED_TITLE = "已占用";

    private MeetingCalendarAssembler() {
    }

    public static List<MeetingCalendarItem> toItems(List<MeetingBooking> bookings,
                                                    Long currentUserId,
                                                    boolean canViewAll) {
        if (bookings == null) {
            return List.of();
        }
        return bookings.stream()
                .filter(Objects::nonNull)
                .map(booking -> toItem(booking, currentUserId, canViewAll))
                .sorted(Comparator.comparing(MeetingCalendarItem::getStartTime,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(MeetingCalendarItem::getRoomName,
                                Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    private static MeetingCalendarItem toItem(MeetingBooking booking, Long currentUserId, boolean canViewAll) {
        MeetingCalendarItem item = new MeetingCalendarItem();
        item.setId(booking.getId());
        item.setBookingNo(booking.getBookingNo());
        item.setRoomId(booking.getRoomId());
        item.setRoomName(booking.getRoomName());
        item.setLocation(booking.getLocation());
        item.setEquipment(booking.getEquipment());
        item.setBookerId(booking.getBookerId());
        item.setBookerName(booking.getBookerName());
        item.setMeetingTitle(booking.getMeetingTitle());
        item.setParticipantCount(booking.getParticipantCount());
        item.setStartTime(booking.getStartTime());
        item.setEndTime(booking.getEndTime());
        item.setStatus(booking.getStatus());
        item.setStatusText(statusText(booking.getStatus()));
        item.setRemark(booking.getRemark());
        boolean own = Objects.equals(currentUserId, booking.getBookerId());
        if (!canViewAll && !own) {
            item.setBookingNo(null);
            item.setBookerId(null);
            item.setBookerName(null);
            item.setMeetingTitle(MASKED_TITLE);
            item.setParticipantCount(null);
            item.setRemark(null);
            item.setMasked(true);
        }
        return item;
    }

    private static String statusText(Integer status) {
        return switch (status == null ? -1 : status) {
            case 0 -> "待开始";
            case 1 -> "进行中";
            case 2 -> "已结束";
            case 3 -> "已取消";
            default -> "未知状态";
        };
    }
}
