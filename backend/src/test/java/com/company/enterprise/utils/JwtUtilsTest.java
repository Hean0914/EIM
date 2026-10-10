package com.company.enterprise.utils;

import com.company.enterprise.common.TokenUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtUtilsTest {

    @Test
    void generateAndParseTokenReturnsOriginalUserClaims() {
        JwtUtils jwtUtils = new JwtUtils("unit-test-secret", 2);
        TokenUser user = new TokenUser(1L, "manager", "Manager", 1L, "GENERAL_MANAGER", 1L);
        user.setAvatar("https://example.com/avatar.png");

        String token = jwtUtils.generateToken(user);
        TokenUser parsed = jwtUtils.parseToken(token);

        assertNotNull(token);
        assertEquals(user.getUserId(), parsed.getUserId());
        assertEquals(user.getUsername(), parsed.getUsername());
        assertEquals(user.getRealName(), parsed.getRealName());
        assertEquals(user.getRoleId(), parsed.getRoleId());
        assertEquals(user.getRoleCode(), parsed.getRoleCode());
        assertEquals(user.getEmployeeId(), parsed.getEmployeeId());
        assertEquals(user.getAvatar(), parsed.getAvatar());
    }
}
