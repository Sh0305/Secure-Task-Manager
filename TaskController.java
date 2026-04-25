package com.securetask.taskmanager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.securetask.taskmanager.dto.PageResponse;
import com.securetask.taskmanager.dto.TaskRequest;
import com.securetask.taskmanager.dto.TaskResponse;
import com.securetask.taskmanager.dto.TaskStatsResponse;
import com.securetask.taskmanager.dto.UserStatsResponse;
import com.securetask.taskmanager.model.AuditLog;
import com.securetask.taskmanager.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // Any logged-in user can create a task
    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.createTask(request));
    }

    // Any logged-in user sees only their own tasks
    @GetMapping("/my")
    public ResponseEntity<PageResponse<TaskResponse>> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(taskService.getMyTasks(page, size, sortBy, direction));
    }

    // Only ADMIN can see all tasks — optionally filter by status
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<TaskResponse>> getAll(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(taskService.getAllTasks(status, page, size, sortBy, direction));
    }

    // Update full task details
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    // Update just the status — eg mark as COMPLETED
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(taskService.updateStatus(id, status));
    }

    // Only ADMIN can delete tasks
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok("Task deleted successfully");
    }

    // Any user can see their own overdue tasks
    @GetMapping("/my/overdue")
    public ResponseEntity<List<TaskResponse>> getMyOverdueTasks() {
        return ResponseEntity.ok(taskService.getMyOverdueTasks());
    }

    // Admin can see all overdue tasks
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<TaskResponse>> getOverdueTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(taskService.getOverdueTasks(page, size));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TaskStatsResponse> getStats() {
        return ResponseEntity.ok(taskService.getTaskStats());
    }

    // Any user can search their own tasks
@GetMapping("/my/search")
public ResponseEntity<PageResponse<TaskResponse>> searchMyTasks(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(taskService.searchMyTasks(keyword, page, size));
}

    // Admin can search all tasks
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<TaskResponse>> searchTasks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(taskService.searchTasks(keyword, page, size));
    }

    // User sees their own upcoming tasks
@GetMapping("/my/upcoming")
public ResponseEntity<PageResponse<TaskResponse>> getMyUpcomingTasks(
        @RequestParam(defaultValue = "7") int days,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(
            taskService.getMyUpcomingTasks(days, page, size));
}

// Admin sees all upcoming tasks
@GetMapping("/upcoming")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<PageResponse<TaskResponse>> getUpcomingTasks(
        @RequestParam(defaultValue = "7") int days,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(
            taskService.getUpcomingTasks(days, page, size));
}
// Any logged-in user can see their own stats
@GetMapping("/my/stats")
public ResponseEntity<UserStatsResponse> getMyStats() {
    return ResponseEntity.ok(taskService.getMyStats());
}
// See full history of a specific task
@GetMapping("/{id}/activity")
public ResponseEntity<List<AuditLog>> getTaskActivity(
        @PathVariable Long id) {
    return ResponseEntity.ok(taskService.getTaskActivity(id));
}
}
