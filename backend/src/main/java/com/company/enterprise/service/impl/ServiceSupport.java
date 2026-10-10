package com.company.enterprise.service.impl;

import com.company.enterprise.common.TokenUser;
import com.company.enterprise.common.UserContext;
import com.company.enterprise.common.exception.BusinessException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

final class ServiceSupport {

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private ServiceSupport() {
    }

    static long normalizeCurrent(Long current) {
        return current == null || current < 1 ? 1 : current;
    }

    static long normalizeSize(Long size) {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }

    static Long requireUserId() {
        TokenUser user = UserContext.get();
        if (user == null || user.getUserId() == null) {
            throw new BusinessException(401, "请先登录");
        }
        return user.getUserId();
    }

    static Long requireEmployeeId() {
        TokenUser user = UserContext.get();
        if (user == null || user.getEmployeeId() == null) {
            throw new BusinessException(401, "当前账号未绑定员工档案");
        }
        return user.getEmployeeId();
    }

    static boolean canManagePeople() {
        return UserContext.canManagePeople();
    }

    static boolean canManagePlans() {
        return UserContext.canManagePlans();
    }

    static boolean canManageOffice() {
        return UserContext.canManageOffice();
    }

    static void requireCurrentEmployeeOrAdmin(Long employeeId, String message) {
        if (!canManagePeople() && !Objects.equals(requireEmployeeId(), employeeId)) {
            throw new BusinessException(403, message);
        }
    }

    static void requireCurrentUserOrAdmin(Long userId, String message) {
        if (!canManagePeople() && !Objects.equals(requireUserId(), userId)) {
            throw new BusinessException(403, message);
        }
    }

    static String businessNo(String prefix) {
        return prefix + LocalDateTime.now().format(NO_FORMATTER);
    }
}
