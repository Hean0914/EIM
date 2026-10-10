package com.company.enterprise.service;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.dto.MeetingBookingQuery;
import com.company.enterprise.dto.MeetingBookingRequest;
import com.company.enterprise.entity.MeetingBooking;
import com.company.enterprise.dto.MeetingCalendarQuery;
import com.company.enterprise.vo.MeetingCalendarItem;

import java.util.List;

public interface MeetingBookingService {

    PageResult<MeetingBooking> page(MeetingBookingQuery query);

    List<MeetingCalendarItem> calendar(MeetingCalendarQuery query);

    MeetingBooking detail(Long id);

    Long create(MeetingBookingRequest request);

    void cancel(Long id);

    void delete(Long id);
}
