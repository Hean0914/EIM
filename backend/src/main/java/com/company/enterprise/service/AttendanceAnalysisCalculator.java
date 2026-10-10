package com.company.enterprise.service;

import com.company.enterprise.entity.AttendanceRecord;
import com.company.enterprise.entity.Employee;
import com.company.enterprise.entity.LeaveApplication;
import com.company.enterprise.entity.OutApplication;
import com.company.enterprise.vo.AttendanceAnalysisResult;
import com.company.enterprise.vo.AttendanceAnomalyDistribution;
import com.company.enterprise.vo.AttendanceCalendarItem;
import com.company.enterprise.vo.AttendanceDailyTrend;
import com.company.enterprise.vo.AttendanceEmployeeRanking;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class AttendanceAnalysisCalculator {

    private static final String NORMAL = "NORMAL";
    private static final String REST = "REST";
    private static final String LEAVE = "LEAVE";
    private static final String OUT = "OUT";
    private static final String ABSENT = "ABSENT";
    private static final String ABNORMAL = "ABNORMAL";
    private static final String MISSING_CHECK_IN = "MISSING_CHECK_IN";
    private static final String MISSING_CHECK_OUT = "MISSING_CHECK_OUT";

    private AttendanceAnalysisCalculator() {
    }

    public static AttendanceAnalysisResult calculate(LocalDate startDate, LocalDate endDate,
                                                     List<Employee> employees,
                                                     List<AttendanceRecord> records,
                                                     List<LeaveApplication> leaves,
                                                     List<OutApplication> outs,
                                                     LocalTime workStart,
                                                     LocalTime workEnd) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("考勤分析日期范围无效");
        }
        List<Employee> safeEmployees = employees == null ? List.of() : employees.stream()
                .filter(Objects::nonNull)
                .filter(employee -> employee.getId() != null)
                .toList();
        Map<String, AttendanceRecord> recordMap = (records == null ? List.<AttendanceRecord>of() : records).stream()
                .filter(Objects::nonNull)
                .filter(record -> record.getEmployeeId() != null && record.getAttendanceDate() != null)
                .collect(Collectors.toMap(record -> key(record.getEmployeeId(), record.getAttendanceDate()),
                        Function.identity(), (left, right) -> right));
        Map<Long, List<LeaveApplication>> leaveMap = groupByEmployee(leaves);
        Map<Long, List<OutApplication>> outMap = groupByEmployee(outs);
        AttendanceAnalysisResult result = new AttendanceAnalysisResult();
        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setEmployeeCount(safeEmployees.size());

        Map<Long, AttendanceEmployeeRanking> rankingMap = new HashMap<>();
        Map<LocalDate, AttendanceDailyTrend> trendMap = new HashMap<>();
        for (Employee employee : safeEmployees) {
            AttendanceEmployeeRanking ranking = createRanking(employee);
            rankingMap.put(employee.getId(), ranking);
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                if (!isActiveOn(employee, date)) {
                    continue;
                }
                AttendanceDailyTrend trend = trendMap.computeIfAbsent(date, key -> {
                    AttendanceDailyTrend value = new AttendanceDailyTrend();
                    value.setDate(key);
                    value.setWorkday(isWorkday(key));
                    return value;
                });
                AttendanceCalendarItem item = createBaseItem(employee, date);
                AttendanceRecord record = recordMap.get(key(employee.getId(), date));
                LeaveApplication leave = findApplication(leaveMap.get(employee.getId()), date);
                OutApplication out = findApplication(outMap.get(employee.getId()), date);
                if (leave != null) {
                    item.setApprovedLeave(true);
                    item.setLeaveType(leave.getLeaveType());
                    item.setStatus(LEAVE);
                    item.setStatusText("请假");
                    trend.setLeaveCount(trend.getLeaveCount() + 1);
                } else if (out != null) {
                    item.setApprovedOut(true);
                    item.setOutDestination(out.getDestination());
                    item.setStatus(OUT);
                    item.setStatusText("外出");
                    trend.setOutCount(trend.getOutCount() + 1);
                } else if (!item.isWorkday()) {
                    item.setStatus(REST);
                    item.setStatusText("休息日");
                } else {
                    result.setShouldWorkDays(result.getShouldWorkDays() + 1);
                    ranking.setShouldWorkDays(ranking.getShouldWorkDays() + 1);
                    updateWorkdayItem(item, record, workStart, workEnd, result, ranking);
                    if (NORMAL.equals(item.getStatus())) {
                        trend.setNormalCount(trend.getNormalCount() + 1);
                    } else if (ABSENT.equals(item.getStatus())) {
                        trend.setAbsentCount(trend.getAbsentCount() + 1);
                    } else {
                        trend.setAnomalyCount(trend.getAnomalyCount() + 1);
                    }
                }
                result.getCalendar().add(item);
            }
        }

        result.getCalendar().sort(Comparator.comparing(AttendanceCalendarItem::getDate)
                .thenComparing(item -> item.getEmployeeId() == null ? 0L : item.getEmployeeId()));
        result.getDailyTrend().addAll(trendMap.values().stream()
                .sorted(Comparator.comparing(AttendanceDailyTrend::getDate)).toList());
        result.getAnomalyDistribution().addAll(List.of(
                distribution("LATE", "迟到", result.getLateCount()),
                distribution("EARLY_LEAVE", "早退", result.getEarlyLeaveCount()),
                distribution("MISSING_CHECK_IN", "上班缺卡", result.getMissingCheckInCount()),
                distribution("MISSING_CHECK_OUT", "下班缺卡", result.getMissingCheckOutCount()),
                distribution("ABSENCE", "缺勤", result.getAbsenceCount()),
                distribution("GPS_ABNORMAL", "GPS 异常", result.getGpsAbnormalCount())
        ));
        result.getEmployeeRanking().addAll(rankingMap.values().stream()
                .peek(ranking -> ranking.setAttendanceRate(rate(ranking.getActualWorkDays(), ranking.getShouldWorkDays())))
                .sorted(Comparator.comparing(AttendanceEmployeeRanking::getAnomalyCount).reversed()
                        .thenComparing(AttendanceEmployeeRanking::getEmployeeName,
                                Comparator.nullsLast(String::compareTo)))
                .toList());
        result.setAttendanceRate(rate(result.getActualWorkDays(), result.getShouldWorkDays()));
        return result;
    }

    private static void updateWorkdayItem(AttendanceCalendarItem item, AttendanceRecord record,
                                          LocalTime workStart, LocalTime workEnd,
                                          AttendanceAnalysisResult result,
                                          AttendanceEmployeeRanking ranking) {
        if (record == null) {
            item.setStatus(ABSENT);
            item.setStatusText("缺勤");
            item.getAnomalies().add("ABSENCE");
            result.setAbsenceCount(result.getAbsenceCount() + 1);
            ranking.setAbsenceCount(ranking.getAbsenceCount() + 1);
            ranking.setAnomalyCount(ranking.getAnomalyCount() + 1);
            return;
        }
        item.setCheckInTime(record.getCheckInTime());
        item.setCheckOutTime(record.getCheckOutTime());
        item.setWorkMinutes(record.getWorkMinutes());
        item.setCheckInStatus(record.getCheckInStatus());
        item.setCheckOutStatus(record.getCheckOutStatus());
        item.setCheckInGpsStatus(record.getCheckInGpsStatus());
        item.setCheckOutGpsStatus(record.getCheckOutGpsStatus());
        item.setCheckInDistance(record.getCheckInDistance());
        item.setCheckOutDistance(record.getCheckOutDistance());
        item.setRemark(record.getRemark());
        if (record.getCheckInTime() != null || record.getCheckOutTime() != null) {
            result.setActualWorkDays(result.getActualWorkDays() + 1);
            ranking.setActualWorkDays(ranking.getActualWorkDays() + 1);
        }
        if (isLate(record, workStart)) {
            addAnomaly(item, "LATE");
            result.setLateCount(result.getLateCount() + 1);
            ranking.setLateCount(ranking.getLateCount() + 1);
        }
        if (isEarlyLeave(record, workEnd)) {
            addAnomaly(item, "EARLY_LEAVE");
            result.setEarlyLeaveCount(result.getEarlyLeaveCount() + 1);
            ranking.setEarlyLeaveCount(ranking.getEarlyLeaveCount() + 1);
        }
        if (record.getCheckInTime() == null) {
            addAnomaly(item, "MISSING_CHECK_IN");
            result.setMissingCheckInCount(result.getMissingCheckInCount() + 1);
            ranking.setMissingCheckInCount(ranking.getMissingCheckInCount() + 1);
        }
        if (record.getCheckOutTime() == null) {
            addAnomaly(item, "MISSING_CHECK_OUT");
            result.setMissingCheckOutCount(result.getMissingCheckOutCount() + 1);
            ranking.setMissingCheckOutCount(ranking.getMissingCheckOutCount() + 1);
        }
        if (isGpsAbnormal(record.getCheckInGpsStatus()) || isGpsAbnormal(record.getCheckOutGpsStatus())) {
            addAnomaly(item, "GPS_ABNORMAL");
            result.setGpsAbnormalCount(result.getGpsAbnormalCount() + 1);
        }
        if (item.getAnomalies().isEmpty()) {
            item.setStatus(NORMAL);
            item.setStatusText("正常");
        } else if (item.getAnomalies().size() == 1 && MISSING_CHECK_IN.equals(item.getAnomalies().get(0))) {
            item.setStatus(MISSING_CHECK_IN);
            item.setStatusText("上班缺卡");
        } else if (item.getAnomalies().size() == 1 && MISSING_CHECK_OUT.equals(item.getAnomalies().get(0))) {
            item.setStatus(MISSING_CHECK_OUT);
            item.setStatusText("下班缺卡");
        } else {
            item.setStatus(ABNORMAL);
            item.setStatusText("存在异常");
        }
        if (!item.getAnomalies().isEmpty()) {
            ranking.setAnomalyCount(ranking.getAnomalyCount() + 1);
        }
    }

    private static AttendanceCalendarItem createBaseItem(Employee employee, LocalDate date) {
        AttendanceCalendarItem item = new AttendanceCalendarItem();
        item.setDate(date);
        item.setWorkday(isWorkday(date));
        item.setEmployeeId(employee.getId());
        item.setEmployeeNo(employee.getEmployeeNo());
        item.setEmployeeName(employee.getEmployeeName());
        item.setDeptId(employee.getDeptId());
        return item;
    }

    private static AttendanceEmployeeRanking createRanking(Employee employee) {
        AttendanceEmployeeRanking ranking = new AttendanceEmployeeRanking();
        ranking.setEmployeeId(employee.getId());
        ranking.setEmployeeNo(employee.getEmployeeNo());
        ranking.setEmployeeName(employee.getEmployeeName());
        return ranking;
    }

    private static boolean isActiveOn(Employee employee, LocalDate date) {
        return (employee.getHireDate() == null || !date.isBefore(employee.getHireDate()))
                && (employee.getResignDate() == null || !date.isAfter(employee.getResignDate()));
    }

    private static boolean isWorkday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    private static boolean isLate(AttendanceRecord record, LocalTime workStart) {
        return record.getCheckInStatus() != null && record.getCheckInStatus() == 2
                || record.getCheckInStatus() == null && record.getCheckInTime() != null
                && workStart != null && record.getCheckInTime().toLocalTime().isAfter(workStart);
    }

    private static boolean isEarlyLeave(AttendanceRecord record, LocalTime workEnd) {
        return record.getCheckOutStatus() != null && record.getCheckOutStatus() == 2
                || record.getCheckOutStatus() == null && record.getCheckOutTime() != null
                && workEnd != null && record.getCheckOutTime().toLocalTime().isBefore(workEnd);
    }

    private static boolean isGpsAbnormal(Integer status) {
        return status != null && (status == 2 || status == 3);
    }

    private static void addAnomaly(AttendanceCalendarItem item, String anomaly) {
        if (!item.getAnomalies().contains(anomaly)) {
            item.getAnomalies().add(anomaly);
        }
    }

    private static <T> Map<Long, List<T>> groupByEmployee(List<T> applications) {
        if (applications == null) {
            return Map.of();
        }
        return applications.stream()
                .filter(Objects::nonNull)
                .filter(application -> employeeId(application) != null && status(application) == 1)
                .collect(Collectors.groupingBy(AttendanceAnalysisCalculator::employeeId));
    }

    private static Long employeeId(Object application) {
        if (application instanceof LeaveApplication leave) {
            return leave.getEmployeeId();
        }
        if (application instanceof OutApplication out) {
            return out.getEmployeeId();
        }
        return null;
    }

    private static Integer status(Object application) {
        if (application instanceof LeaveApplication leave) {
            return leave.getStatus();
        }
        if (application instanceof OutApplication out) {
            return out.getStatus();
        }
        return null;
    }

    private static <T> T findApplication(List<T> applications, LocalDate date) {
        if (applications == null) {
            return null;
        }
        return applications.stream().filter(application -> {
            LocalDateTime start;
            LocalDateTime end;
            if (application instanceof LeaveApplication leave) {
                start = leave.getStartTime();
                end = leave.getEndTime();
            } else if (application instanceof OutApplication out) {
                start = out.getStartTime();
                end = out.getEndTime();
            } else {
                return false;
            }
            return start != null && end != null && !date.isBefore(start.toLocalDate()) && !date.isAfter(end.toLocalDate());
        }).findFirst().orElse(null);
    }

    private static AttendanceAnomalyDistribution distribution(String type, String label, int count) {
        return new AttendanceAnomalyDistribution(type, label, count);
    }

    private static int rate(int actual, int should) {
        return should == 0 ? 0 : Math.round(actual * 100f / should);
    }

    private static String key(Long employeeId, LocalDate date) {
        return employeeId + "@" + date;
    }
}
