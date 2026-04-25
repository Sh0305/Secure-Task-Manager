package com.securetask.taskmanager.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskStatsResponse {
    private long totalTasks;
    private long pendingTasks;
    private long inProgressTasks;
    private long completedTasks;
    private long overdueTasks;
    private double completionRate;   // percentage of tasks completed
}
