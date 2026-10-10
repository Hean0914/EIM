package com.company.enterprise.service;

import com.company.enterprise.entity.AttendanceRecord;
import com.company.enterprise.entity.AttendanceStat;
import com.company.enterprise.entity.Department;
import com.company.enterprise.entity.Employee;
import com.company.enterprise.entity.MeetingBooking;
import com.company.enterprise.entity.OutApplication;
import com.company.enterprise.entity.Plan;
import com.company.enterprise.entity.Position;
import com.company.enterprise.entity.Role;
import com.company.enterprise.entity.User;
import com.company.enterprise.vo.EmployeeProfileVO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class EmployeeProfileAssembler {

    public EmployeeProfileVO assemble(Employee employee, Department department, Position position,
                                     User user, Role role, AttendanceStat attendanceStat,
                                     List<AttendanceRecord> attendanceRecords,
                                     List<com.company.enterprise.entity.LeaveApplication> leaves,
                                     List<OutApplication> outs, List<Plan> plans,
                                     List<MeetingBooking> meetings, LocalDate today) {
        EmployeeProfileVO profile = new EmployeeProfileVO();
        fillEmployee(profile.getEmployee(), employee, department, position, today);
        fillAccount(profile.getAccount(), user, role);
        fillMetrics(profile.getMetrics(), attendanceStat, attendanceRecords, leaves, outs, plans, meetings, today);
        profile.setAttendanceTrend(toAttendanceTrend(attendanceRecords));
        profile.setPlans(toPlans(plans));
        profile.setMeetings(toMeetings(meetings));
        profile.setTimeline(buildTimeline(employee, attendanceRecords, leaves, outs, plans, meetings));
        return profile;
    }

    private void fillEmployee(EmployeeProfileVO.EmployeeInfo view, Employee employee, Department department,
                              Position position, LocalDate today) {
        view.setId(employee.getId());
        view.setUserId(employee.getUserId());
        view.setEmployeeNo(employee.getEmployeeNo());
        view.setEmployeeName(employee.getEmployeeName());
        view.setGender(employee.getGender());
        view.setDeptId(employee.getDeptId());
        view.setDeptName(department == null ? null : department.getDeptName());
        view.setPositionId(employee.getPositionId());
        view.setPositionName(position == null ? null : position.getPositionName());
        view.setPhone(employee.getPhone());
        view.setEmail(employee.getEmail());
        view.setHireDate(employee.getHireDate());
        view.setRegularDate(employee.getRegularDate());
        view.setResignDate(employee.getResignDate());
        view.setEmploymentStatus(employee.getEmploymentStatus());
        view.setEmploymentStatusLabel(statusLabel(employee.getEmploymentStatus()));
        view.setServiceYears(employee.getHireDate() == null || today == null
                ? 0 : Math.max(0, Period.between(employee.getHireDate(), today).getYears()));
        view.setIdCard(maskIdCard(employee.getIdCard()));
        view.setAddress(employee.getAddress());
        view.setEmergencyContact(employee.getEmergencyContact());
        view.setEmergencyPhone(maskPhone(employee.getEmergencyPhone()));
        view.setRemark(employee.getRemark());
    }

    private void fillAccount(EmployeeProfileVO.AccountInfo view, User user, Role role) {
        view.setAccountAssigned(user != null);
        if (user != null) {
            view.setUsername(user.getUsername());
            view.setAccountStatus(user.getStatus());
        }
        if (role != null) {
            view.setRoleCode(role.getRoleCode());
            view.setRoleName(role.getRoleName());
        }
        view.setPasswordExposed(false);
    }

    private void fillMetrics(EmployeeProfileVO.Metrics metrics, AttendanceStat stat,
                             List<AttendanceRecord> records,
                             List<com.company.enterprise.entity.LeaveApplication> leaves,
                             List<OutApplication> outs, List<Plan> plans,
                             List<MeetingBooking> meetings, LocalDate today) {
        if (stat != null) {
            int should = decimalInt(stat.getShouldWorkDays());
            int actual = decimalInt(stat.getActualWorkDays());
            metrics.setShouldWorkDays(should);
            metrics.setActualWorkDays(actual);
            metrics.setAttendanceRate(should == 0 ? 0 : BigDecimal.valueOf(actual * 100L)
                    .divide(BigDecimal.valueOf(should), 0, RoundingMode.HALF_UP).intValue());
            metrics.setLateCount(defaultInt(stat.getLateCount()));
            metrics.setEarlyLeaveCount(defaultInt(stat.getEarlyLeaveCount()));
            metrics.setAbsenceCount(defaultInt(stat.getAbsenceCount()));
            metrics.setLeaveDays(defaultDecimal(stat.getLeaveDays()));
            metrics.setOutDays(defaultDecimal(stat.getOutDays()));
        } else {
            calculateFromRecords(metrics, records);
        }
        List<OutApplication> safeOuts = safeList(outs);
        metrics.setOutCount(safeOuts.size());
        metrics.setActivePlanCount((int) safeList(plans).stream().filter(plan -> plan != null && plan.getStatus() != null
                && plan.getStatus() == 1).count());
        metrics.setUpcomingMeetingCount((int) safeList(meetings).stream().filter(meeting -> meeting != null
                && meeting.getStartTime() != null && (today == null || !meeting.getStartTime().toLocalDate().isBefore(today))
                && meeting.getStatus() != null && meeting.getStatus() != 3).count());
    }

    private void calculateFromRecords(EmployeeProfileVO.Metrics metrics, List<AttendanceRecord> records) {
        int late = 0;
        int early = 0;
        int actual = 0;
        for (AttendanceRecord record : safeList(records)) {
            if (record == null) {
                continue;
            }
            if (record.getCheckInTime() != null || record.getCheckOutTime() != null) {
                actual++;
            }
            if (Objects.equals(record.getCheckInStatus(), 2)) {
                late++;
            }
            if (Objects.equals(record.getCheckOutStatus(), 2)) {
                early++;
            }
        }
        metrics.setActualWorkDays(actual);
        metrics.setLateCount(late);
        metrics.setEarlyLeaveCount(early);
    }

    private List<EmployeeProfileVO.AttendanceTrendItem> toAttendanceTrend(List<AttendanceRecord> records) {
        return safeList(records).stream().filter(Objects::nonNull).filter(record -> record.getAttendanceDate() != null)
                .sorted(Comparator.comparing(AttendanceRecord::getAttendanceDate))
                .map(record -> {
                    EmployeeProfileVO.AttendanceTrendItem item = new EmployeeProfileVO.AttendanceTrendItem();
                    item.setDate(record.getAttendanceDate());
                    item.setCheckInTime(record.getCheckInTime());
                    item.setCheckOutTime(record.getCheckOutTime());
                    item.setWorkMinutes(record.getWorkMinutes());
                    if (record.getCheckInTime() == null && record.getCheckOutTime() == null) {
                        item.setStatus("ABSENT");
                        item.setStatusText("缺勤");
                        item.getAnomalies().add("ABSENCE");
                    } else if (Objects.equals(record.getCheckInStatus(), 2) || Objects.equals(record.getCheckOutStatus(), 2)) {
                        item.setStatus("ABNORMAL");
                        item.setStatusText("存在异常");
                        if (Objects.equals(record.getCheckInStatus(), 2)) item.getAnomalies().add("LATE");
                        if (Objects.equals(record.getCheckOutStatus(), 2)) item.getAnomalies().add("EARLY_LEAVE");
                    } else {
                        item.setStatus("NORMAL");
                        item.setStatusText("正常");
                    }
                    return item;
                }).toList();
    }

    private List<EmployeeProfileVO.PlanItem> toPlans(List<Plan> plans) {
        return safeList(plans).stream().filter(Objects::nonNull).sorted(Comparator.comparing(Plan::getEndDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(plan -> {
                    EmployeeProfileVO.PlanItem item = new EmployeeProfileVO.PlanItem();
                    item.setId(plan.getId());
                    item.setPlanNo(plan.getPlanNo());
                    item.setTitle(plan.getTitle());
                    item.setStartDate(plan.getStartDate());
                    item.setEndDate(plan.getEndDate());
                    item.setProgress(plan.getProgress());
                    item.setPriority(plan.getPriority());
                    item.setStatus(plan.getStatus());
                    return item;
                }).toList();
    }

    private List<EmployeeProfileVO.MeetingItem> toMeetings(List<MeetingBooking> meetings) {
        return safeList(meetings).stream().filter(Objects::nonNull).sorted(Comparator.comparing(MeetingBooking::getStartTime,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(meeting -> {
                    EmployeeProfileVO.MeetingItem item = new EmployeeProfileVO.MeetingItem();
                    item.setId(meeting.getId());
                    item.setMeetingTitle(meeting.getMeetingTitle());
                    item.setRoomName(meeting.getRoomName());
                    item.setParticipantCount(meeting.getParticipantCount());
                    item.setStartTime(meeting.getStartTime());
                    item.setEndTime(meeting.getEndTime());
                    item.setStatus(meeting.getStatus());
                    return item;
                }).toList();
    }

    private List<EmployeeProfileVO.TimelineItem> buildTimeline(Employee employee, List<AttendanceRecord> records,
                                                               List<com.company.enterprise.entity.LeaveApplication> leaves,
                                                               List<OutApplication> outs, List<Plan> plans,
                                                               List<MeetingBooking> meetings) {
        List<EmployeeProfileVO.TimelineItem> timeline = new java.util.ArrayList<>();
        if (employee.getHireDate() != null) {
            timeline.add(timeline("hire", "employee", "入职", "加入企业", employee.getHireDate().atStartOfDay(), null));
        }
        safeList(records).stream().filter(Objects::nonNull).forEach(record -> timeline.add(timeline(
                "attendance-" + record.getId(), "attendance", "考勤记录",
                attendanceDescription(record), dateTime(record.getCheckInTime(), record.getAttendanceDate()),
                attendanceStatus(record))));
        safeList(leaves).stream().filter(Objects::nonNull).forEach(leave -> timeline.add(timeline(
                "leave-" + leave.getId(), "approval", "请假申请", leave.getReason(),
                firstTime(leave.getCreateTime(), leave.getStartTime()), approvalStatus(leave.getStatus()))));
        safeList(outs).stream().filter(Objects::nonNull).forEach(out -> timeline.add(timeline(
                "out-" + out.getId(), "approval", "外出申请", out.getDestination(),
                firstTime(out.getCreateTime(), out.getStartTime()), approvalStatus(out.getStatus()))));
        safeList(plans).stream().filter(Objects::nonNull).forEach(plan -> timeline.add(timeline(
                "plan-" + plan.getId(), "plan", "计划更新", plan.getTitle(),
                firstTime(plan.getCreateTime(), plan.getStartDate() == null ? null : plan.getStartDate().atStartOfDay()),
                planStatus(plan.getStatus()))));
        safeList(meetings).stream().filter(Objects::nonNull).forEach(meeting -> timeline.add(timeline(
                "meeting-" + meeting.getId(), "meeting", "会议安排", meeting.getMeetingTitle(),
                firstTime(meeting.getCreateTime(), meeting.getStartTime()), meetingStatus(meeting.getStatus()))));
        timeline.sort(Comparator.comparing(EmployeeProfileVO.TimelineItem::getTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return timeline;
    }

    private EmployeeProfileVO.TimelineItem timeline(String key, String type, String title, String description,
                                                    LocalDateTime time, String status) {
        EmployeeProfileVO.TimelineItem item = new EmployeeProfileVO.TimelineItem();
        item.setKey(key);
        item.setType(type);
        item.setTitle(title);
        item.setDescription(description);
        item.setTime(time);
        item.setStatus(status);
        return item;
    }

    private LocalDateTime dateTime(LocalDateTime value, LocalDate date) {
        return value != null ? value : date == null ? null : date.atStartOfDay();
    }

    private LocalDateTime firstTime(LocalDateTime primary, LocalDateTime fallback) {
        return primary != null ? primary : fallback;
    }

    private LocalDateTime firstTime(LocalDateTime primary, LocalDate fallback) {
        return primary != null ? primary : fallback == null ? null : fallback.atStartOfDay();
    }

    private String attendanceDescription(AttendanceRecord record) {
        return record.getRemark() == null ? record.getAttendanceDate() + " 打卡" : record.getRemark();
    }

    private String attendanceStatus(AttendanceRecord record) {
        return Objects.equals(record.getCheckInStatus(), 2) || Objects.equals(record.getCheckOutStatus(), 2)
                ? "异常" : "正常";
    }

    private String approvalStatus(Integer status) {
        return switch (status == null ? -1 : status) {
            case 0 -> "待审批";
            case 1 -> "已通过";
            case 2 -> "已驳回";
            case 3 -> "已撤销";
            default -> "未知";
        };
    }

    private String planStatus(Integer status) {
        return switch (status == null ? -1 : status) {
            case 0 -> "未开始";
            case 1 -> "进行中";
            case 2 -> "已完成";
            case 3 -> "已延期";
            case 4 -> "已取消";
            default -> "未知";
        };
    }

    private String meetingStatus(Integer status) {
        return switch (status == null ? -1 : status) {
            case 0 -> "待开始";
            case 1 -> "进行中";
            case 2 -> "已结束";
            case 3 -> "已取消";
            default -> "未知";
        };
    }

    private String statusLabel(Integer status) {
        return switch (status == null ? -1 : status) {
            case 0 -> "离职";
            case 1 -> "在职";
            case 2 -> "试用";
            case 3 -> "停职";
            default -> "未知";
        };
    }

    private String maskIdCard(String value) {
        if (value == null || value.length() <= 7) return value;
        return value.substring(0, 3) + "*".repeat(value.length() - 7) + value.substring(value.length() - 4);
    }

    private String maskPhone(String value) {
        if (value == null || value.length() <= 7) return value;
        return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
    }

    private int decimalInt(BigDecimal value) {
        return value == null ? 0 : value.intValue();
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
}
