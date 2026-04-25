package com.securetask.taskmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.securetask.taskmanager.model.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // All actions performed by a specific user
    List<AuditLog> findByPerformedByOrderByPerformedAtDesc(String email);

    // Full history of a specific task
    List<AuditLog> findByEntityTypeAndEntityIdOrderByPerformedAtDesc(
            String entityType, Long entityId);
}