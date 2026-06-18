package com.securetask.taskmanager.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.securetask.taskmanager.model.AuditLog;
import com.securetask.taskmanager.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(String action, String entityType, Long entityId, String details) {

        // Get the logged-in user's email from the JWT token
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        AuditLog log = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .performedBy(email)
                .details(details)
                .build();

        auditLogRepository.save(log);
    }
}
