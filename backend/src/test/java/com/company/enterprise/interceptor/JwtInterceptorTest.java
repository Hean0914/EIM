package com.company.enterprise.interceptor;

import com.company.enterprise.common.TokenUser;
import com.company.enterprise.common.annotation.RequireAdmin;
import com.company.enterprise.common.annotation.RequireRoles;
import com.company.enterprise.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtInterceptorTest {

    @Test
    void preHandleRejectsNonGeneralManagerWhenHandlerRequiresAdmin() throws Exception {
        JwtInterceptor interceptor = new JwtInterceptor(new JwtUtils("unit-test-secret", 2), new ObjectMapper());
        TokenUser employee = new TokenUser(2L, "employee", "Employee", 3L, "RND_ENGINEER", 2L);

        MockHttpServletRequest request = authorizedRequest(interceptor, employee);
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean allowed = interceptor.preHandle(request, response, handlerMethod("generalManagerOnly"));

        assertFalse(allowed);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

    @Test
    void preHandleAllowsRoleIncludedInRequireRoles() throws Exception {
        JwtInterceptor interceptor = new JwtInterceptor(new JwtUtils("unit-test-secret", 2), new ObjectMapper());
        TokenUser hrManager = new TokenUser(2L, "hr", "HR", 2L, "HR_MANAGER", 2L);

        MockHttpServletRequest request = authorizedRequest(interceptor, hrManager);
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean allowed = interceptor.preHandle(request, response, handlerMethod("hrOrGeneralManager"));

        assertTrue(allowed);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    void preHandleRejectsRoleNotIncludedInRequireRoles() throws Exception {
        JwtInterceptor interceptor = new JwtInterceptor(new JwtUtils("unit-test-secret", 2), new ObjectMapper());
        TokenUser employee = new TokenUser(3L, "developer", "Developer", 3L, "RND_ENGINEER", 3L);

        MockHttpServletRequest request = authorizedRequest(interceptor, employee);
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean allowed = interceptor.preHandle(request, response, handlerMethod("hrOrGeneralManager"));

        assertFalse(allowed);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

    private MockHttpServletRequest authorizedRequest(JwtInterceptor interceptor, TokenUser user) {
        JwtUtils jwtUtils = new JwtUtils("unit-test-secret", 2);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwtUtils.generateToken(user));
        return request;
    }

    private HandlerMethod handlerMethod(String methodName) throws NoSuchMethodException {
        Method method = ProtectedHandler.class.getDeclaredMethod(methodName);
        return new HandlerMethod(new ProtectedHandler(), method);
    }

    private static class ProtectedHandler {

        @RequireAdmin
        void generalManagerOnly() {
        }

        @RequireRoles({"GENERAL_MANAGER", "HR_MANAGER"})
        void hrOrGeneralManager() {
        }
    }
}
