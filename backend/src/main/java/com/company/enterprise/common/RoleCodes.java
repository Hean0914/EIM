package com.company.enterprise.common;

public final class RoleCodes {

    public static final String GENERAL_MANAGER = "GENERAL_MANAGER";
    public static final String HR_MANAGER = "HR_MANAGER";
    public static final String RND_ENGINEER = "RND_ENGINEER";
    public static final String SALES_SPECIALIST = "SALES_SPECIALIST";
    public static final String ACCOUNTANT = "ACCOUNTANT";

    public static String forPositionCode(String positionCode) {
        if (positionCode == null) {
            return null;
        }
        return switch (positionCode) {
            case GENERAL_MANAGER -> GENERAL_MANAGER;
            case HR_MANAGER -> HR_MANAGER;
            case "DEVELOPER" -> RND_ENGINEER;
            case "SALES" -> SALES_SPECIALIST;
            case ACCOUNTANT -> ACCOUNTANT;
            default -> null;
        };
    }

    private RoleCodes() {
    }
}
