package com.company.enterprise.aspect;

import com.company.enterprise.common.TokenUser;
import com.company.enterprise.common.UserContext;
import com.company.enterprise.common.annotation.Audit;
import com.company.enterprise.entity.AuditLog;
import com.company.enterprise.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
@Component
public class AuditLogAspect {

    private static final Pattern NUMERIC_ID = Pattern.compile("(?:^|/)\\d+(?:$|/)");

    private final AuditLogService auditLogService;
    private final HttpServletRequest request;

    public AuditLogAspect(AuditLogService auditLogService, HttpServletRequest request) {
        this.auditLogService = auditLogService;
        this.request = request;
    }

    @Around("@annotation(com.company.enterprise.common.annotation.Audit)")
    public Object record(ProceedingJoinPoint joinPoint) throws Throwable {
        Audit audit = ((MethodSignature) joinPoint.getSignature()).getMethod()
                .getAnnotation(Audit.class);
        Throwable failure = null;
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable throwable) {
            failure = throwable;
            throw throwable;
        } finally {
            writeLog(audit, joinPoint, failure);
        }
    }

    private void writeLog(Audit audit, ProceedingJoinPoint joinPoint, Throwable failure) {
        TokenUser user = UserContext.get();
        if (audit == null || user == null || user.getUserId() == null) {
            return;
        }
        AuditLog log = new AuditLog();
        log.setUserId(user.getUserId());
        log.setUsername(user.getUsername());
        log.setRealName(user.getRealName());
        log.setRoleCode(user.getRoleCode());
        log.setModule(audit.module());
        log.setAction(audit.action());
        log.setTargetType(audit.targetType());
        log.setTargetId(resolveTargetId(joinPoint));
        log.setRequestMethod(request.getMethod());
        log.setRequestPath(request.getRequestURI());
        log.setOperationStatus(failure == null ? 1 : 0);
        log.setDetail(failure == null ? "操作成功" : "操作失败：" + safeMessage(failure));
        log.setClientIp(request.getRemoteAddr());
        log.setUserAgent(limit(request.getHeader("User-Agent"), 255));
        try {
            auditLogService.record(log);
        } catch (RuntimeException ignored) {
            // Audit failure must not change the business response.
        }
    }

    private Long resolveTargetId(ProceedingJoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Long value) {
                return value;
            }
            if (arg instanceof Integer value) {
                return value.longValue();
            }
        }
        Matcher matcher = NUMERIC_ID.matcher(request.getRequestURI());
        Long id = null;
        while (matcher.find()) {
            String value = matcher.group().replace("/", "");
            id = Long.valueOf(value);
        }
        return id;
    }

    private String safeMessage(Throwable failure) {
        String message = failure.getMessage();
        return limit(message == null ? failure.getClass().getSimpleName() : message, 500);
    }

    private String limit(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }
}
