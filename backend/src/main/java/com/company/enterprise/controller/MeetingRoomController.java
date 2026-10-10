package com.company.enterprise.controller;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.Result;
import com.company.enterprise.common.annotation.RequireRoles;
import static com.company.enterprise.common.RoleCodes.GENERAL_MANAGER;
import static com.company.enterprise.common.RoleCodes.HR_MANAGER;
import com.company.enterprise.dto.MeetingRoomQuery;
import com.company.enterprise.dto.MeetingRoomRequest;
import com.company.enterprise.entity.MeetingRoom;
import com.company.enterprise.service.MeetingRoomService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/meeting-rooms")
public class MeetingRoomController {

    private final MeetingRoomService meetingRoomService;

    public MeetingRoomController(MeetingRoomService meetingRoomService) {
        this.meetingRoomService = meetingRoomService;
    }

    @GetMapping("/page")
    public Result<PageResult<MeetingRoom>> page(@Valid MeetingRoomQuery query) {
        return Result.success(meetingRoomService.page(query));
    }

    @GetMapping("/available")
    public Result<List<MeetingRoom>> availableList() {
        return Result.success(meetingRoomService.availableList());
    }

    @GetMapping("/{id}")
    public Result<MeetingRoom> detail(@PathVariable Long id) {
        return Result.success(meetingRoomService.detail(id));
    }

    @PostMapping
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    public Result<Long> create(@Valid @RequestBody MeetingRoomRequest request) {
        return Result.success(meetingRoomService.create(request));
    }

    @PutMapping
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    public Result<Void> update(@Valid @RequestBody MeetingRoomRequest request) {
        meetingRoomService.update(request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    public Result<Void> delete(@PathVariable Long id) {
        meetingRoomService.delete(id);
        return Result.success();
    }
}
