package com.securetask.taskmanager.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Pattern(
        regexp = "^(LOW|MEDIUM|HIGH)$",
        message = "Priority must be LOW, MEDIUM, or HIGH"
    )
    private String priority;

    @FutureOrPresent(message = "Deadline cannot be in the past")
    private LocalDate deadline;

    private Long assignedToId;
}