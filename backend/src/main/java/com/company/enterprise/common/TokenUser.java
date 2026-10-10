package com.company.enterprise.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenUser {

    private Long userId;
    private String username;
    private String realName;
    private Long roleId;
    private String roleCode;
    private Long employeeId;
    private String avatar;

    public TokenUser(Long userId, String username, String realName, Long roleId, String roleCode, Long employeeId) {
        this(userId, username, realName, roleId, roleCode, employeeId, null);
    }

    public TokenUser(Long userId, String username, String realName, Long roleId, Long employeeId) {
        this(userId, username, realName, roleId, null, employeeId, null);
    }
}
