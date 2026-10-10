package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.dto.MeetingCalendarQuery;
import com.company.enterprise.dto.MeetingBookingQuery;
import com.company.enterprise.entity.MeetingBooking;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MeetingBookingMapper extends BaseMapper<MeetingBooking> {

    List<MeetingBooking> selectBookingPage(@Param("query") MeetingBookingQuery query,
                                           @Param("offset") Long offset,
                                           @Param("size") Long size);

    Long countBookingPage(@Param("query") MeetingBookingQuery query);

    List<MeetingBooking> selectCalendar(@Param("query") MeetingCalendarQuery query);

    MeetingBooking selectBookingById(@Param("id") Long id);

    Long countConflict(MeetingBooking meetingBooking);

    int insertBooking(MeetingBooking meetingBooking);

    int updateBookingStatus(@Param("id") Long id, @Param("status") Integer status);

    int logicDeleteBookingById(@Param("id") Long id);
}
