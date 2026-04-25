package com.securetask.taskmanager.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDate deadline;
    private LocalDateTime createdAt;
    private String createdByName;
    private String assignedToName;

    // Calculated field — not stored in DB
    // true if deadline has passed and task is not completed
    private boolean overdue;

    // How many days until deadline — negative means past due
    private long daysUntilDeadline;
}