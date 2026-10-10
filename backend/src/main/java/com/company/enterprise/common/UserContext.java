package com.company.enterprise.common;

import com.company.enterprise.common.exception.BusinessException;

public class UserContext {

    private static final ThreadLocal<TokenUser> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(TokenUser user) {
        CURRENT_USER.set(user);
    }

    public static TokenUser get() {
        return CURRENT_USER.get();
    }

    public static Long getUserId() {
        TokenUser user = get();
        return user == null ? null : user.getUserId();
    }

    public static Long requireUserId() {
        Long userId = getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }

    public static Long getEmployeeId() {
        TokenUser user = get();
        return user == null ? null : user.getEmployeeId();
    }

    public static Long requireEmployeeId() {
        Long employeeId = getEmployeeId();
        if (employeeId == null) {
            throw new BusinessException(401, "当前账号未绑定员工档案");
        }
        return employeeId;
    }

    public static boolean hasRole(String roleCode) {
        TokenUser user = get();
        return user != null && roleCode != null && roleCode.equals(user.getRoleCode());
    }

    public static boolean hasAnyRole(String... roleCodes) {
        if (roleCodes == null) {
            return false;
        }
        for (String roleCode : roleCodes) {
            if (hasRole(roleCode)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isGeneralManager() {
        return hasRole(RoleCodes.GENERAL_MANAGER);
    }

    public static boolean isHrManager() {
        return hasRole(RoleCodes.HR_MANAGER);
    }

    public static boolean canManagePeople() {
        return hasAnyRole(RoleCodes.GENERAL_MANAGER, RoleCodes.HR_MANAGER);
    }

    public static boolean canApprove() {
        return canManagePeople();
    }

    public static boolean canManageOffice() {
        return canManagePeople();
    }

    public static boolean canManagePlans() {
        return canManagePeople();
    }

    /**
     * @deprecated Use a named permission method such as {@link #canManagePeople()}.
     */
    @Deprecated
    public static boolean isAdmin() {
        return isGeneralManager();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
