package com.ng_doanh.hr_management_system.audit.aspect;

import com.ng_doanh.hr_management_system.audit.annotation.Audited;
import com.ng_doanh.hr_management_system.audit.entity.AuditLog;
import com.ng_doanh.hr_management_system.audit.repository.AuditLogRepository;
import com.ng_doanh.hr_management_system.common.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogRepository auditLogRepository;

    @AfterReturning(value = "@annotation(audited)", returning = "result")
    public void logAuditActivity(JoinPoint joinPoint, Audited audited, Object result) {
        try {
            Long userId = null;
            String username = "ANONYMOUS";

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                userId = userDetails.getId();
                username = userDetails.getUsername();
            }

            String ipAddress = "UNKNOWN";
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ipAddress = request.getRemoteAddr();
            }

            String params = Arrays.toString(joinPoint.getArgs());
            if (params.length() > 500) {
                params = params.substring(0, 500) + "...";
            }

            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .username(username)
                    .action(audited.action())
                    .entityName(audited.entity())
                    .details("Method: " + joinPoint.getSignature().getName() + " | Params: " + params)
                    .ipAddress(ipAddress)
                    .createdAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit log saved: User [{}] performed [{}] on [{}]", username, audited.action(), audited.entity());
        } catch (Exception e) {
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }
}
