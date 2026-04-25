package com.securetask.taskmanager.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatsResponse {
    private String userName;
    private String email;
    private long totalAssigned;
    private long completed;
    private long pending;
    private long inProgress;
    private long overdue;
    private double completionRate;
}