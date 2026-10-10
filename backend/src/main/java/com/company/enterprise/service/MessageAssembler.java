package com.company.enterprise.service;

import com.company.enterprise.entity.Message;
import com.company.enterprise.vo.MessageSummaryVO;

import java.util.Comparator;
import java.util.List;

public final class MessageAssembler {

    private MessageAssembler() {
    }

    public static List<Message> order(List<Message> messages) {
        return (messages == null ? List.<Message>of() : messages).stream()
                .sorted(Comparator.comparing(Message::getPriority, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Message::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public static MessageSummaryVO summarize(List<Message> messages) {
        MessageSummaryVO summary = new MessageSummaryVO();
        List<Message> records = messages == null ? List.of() : messages;
        summary.setTotal((long) records.size());
        summary.setUnread(records.stream().filter(item -> Integer.valueOf(0).equals(item.getReadStatus())).count());
        summary.setHighPriority(records.stream()
                .filter(item -> Integer.valueOf(3).equals(item.getPriority()))
                .count());
        summary.setNoticeCount(countType(records, "NOTICE"));
        summary.setApprovalCount(countType(records, "APPROVAL"));
        summary.setAttendanceCount(countType(records, "ATTENDANCE"));
        summary.setPlanCount(countType(records, "PLAN"));
        summary.setMeetingCount(countType(records, "MEETING"));
        return summary;
    }

    public static String sourceKey(String sourceType, Object sourceId) {
        return String.valueOf(sourceType) + ":" + String.valueOf(sourceId);
    }

    private static long countType(List<Message> records, String type) {
        return records.stream().filter(item -> type.equals(item.getMessageType())).count();
    }
}
