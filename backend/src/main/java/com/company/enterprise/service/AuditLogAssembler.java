package com.company.enterprise.service;

public final class AuditLogAssembler {

    private AuditLogAssembler() {
    }

    public static String statusLabel(Integer status) {
        if (Integer.valueOf(1).equals(status)) return "成功";
        if (Integer.valueOf(0).equals(status)) return "失败";
        return "未知";
    }

    public static String sanitizeDetail(String detail) {
        if (detail == null) return null;
        return detail.replaceAll("(?i)(password|token|secret|authorization)\\s*=\\s*[^\\s,;]+", "$1=[REDACTED]");
    }
}
