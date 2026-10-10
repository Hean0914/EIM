package com.company.enterprise.controller;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.Result;
import com.company.enterprise.common.UserContext;
import com.company.enterprise.common.annotation.RequireRoles;
import com.company.enterprise.common.annotation.Audit;
import static com.company.enterprise.common.RoleCodes.GENERAL_MANAGER;
import static com.company.enterprise.common.RoleCodes.HR_MANAGER;
import com.company.enterprise.dto.MeetingBookingQuery;
import com.company.enterprise.dto.MeetingBookingRequest;
import com.company.enterprise.dto.MeetingCalendarQuery;
import com.company.enterprise.entity.MeetingBooking;
import com.company.enterprise.service.MeetingBookingService;
import com.company.enterprise.vo.MeetingCalendarItem;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/meeting-bookings")
public class MeetingBookingController {

    private final MeetingBookingService meetingBookingService;

    public MeetingBookingController(MeetingBookingService meetingBookingService) {
        this.meetingBookingService = meetingBookingService;
    }

    @GetMapping("/page")
    public Result<PageResult<MeetingBooking>> page(@Valid MeetingBookingQuery query) {
        if (!UserContext.canManageOffice()) {
            query.setBookerId(UserContext.requireUserId());
        }
        return Result.success(meetingBookingService.page(query));
    }

    @GetMapping("/calendar")
    public Result<List<MeetingCalendarItem>> calendar(@Valid MeetingCalendarQuery query) {
        return Result.success(meetingBookingService.calendar(query));
    }

    @GetMapping("/{id}")
    public Result<MeetingBooking> detail(@PathVariable Long id) {
        return Result.success(meetingBookingService.detail(id));
    }

    @PostMapping
    @Audit(module = "MEETING", action = "CREATE", targetType = "MEETING_BOOKING")
    public Result<Long> create(@Valid @RequestBody MeetingBookingRequest request) {
        return Result.success(meetingBookingService.create(request));
    }

    @PostMapping("/{id}/cancel")
    @Audit(module = "MEETING", action = "CANCEL", targetType = "MEETING_BOOKING")
    public Result<Void> cancel(@PathVariable Long id) {
        meetingBookingService.cancel(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    @Audit(module = "MEETING", action = "DELETE", targetType = "MEETING_BOOKING")
    public Result<Void> delete(@PathVariable Long id) {
        meetingBookingService.delete(id);
        return Result.success();
    }
}
