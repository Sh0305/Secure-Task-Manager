package com.securetask.taskmanager.service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.securetask.taskmanager.dto.PageResponse;
import com.securetask.taskmanager.dto.TaskRequest;
import com.securetask.taskmanager.dto.TaskResponse;
import com.securetask.taskmanager.dto.TaskStatsResponse;
import com.securetask.taskmanager.dto.UserStatsResponse;
import com.securetask.taskmanager.exception.InvalidRequestException;
import com.securetask.taskmanager.exception.ResourceNotFoundException;
import com.securetask.taskmanager.model.AuditLog;
import com.securetask.taskmanager.model.Task;
import com.securetask.taskmanager.model.User;
import com.securetask.taskmanager.repository.AuditLogRepository;
import com.securetask.taskmanager.repository.TaskRepository;
import com.securetask.taskmanager.repository.UserRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public TaskResponse createTask(TaskRequest request) {
        User creator = getCurrentUser();

        User assignee = null;
        if (request.getAssignedToId() != null) {
            assignee = userRepository.findById(request.getAssignedToId())
        .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssignedToId()));

        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null
                        ? Task.Priority.valueOf(request.getPriority().toUpperCase())
                        : Task.Priority.MEDIUM)
                .deadline(request.getDeadline())
                .createdBy(creator)
                .assignedTo(assignee)
                .build();

        Task saved = taskRepository.save(task);
        auditService.log("CREATE", "TASK", saved.getId(),
                "Task '" + saved.getTitle() + "' created");
        return toResponse(saved);
    }

    public PageResponse<TaskResponse> getMyTasks(int page, int size,String sortBy, String direction) {
    User currentUser = getCurrentUser();

    Sort sort = direction.equalsIgnoreCase("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

    Pageable pageable = PageRequest.of(page, size, sort);

    // Get assigned and created tasks separately then combine
    Page<Task> assignedTasks = taskRepository.findByAssignedTo(currentUser, pageable);
    Page<Task> createdTasks = taskRepository.findByCreatedBy(currentUser, pageable);

    // Use assigned tasks page — if empty use created tasks page
    Page<Task> result = assignedTasks.isEmpty() ? createdTasks : assignedTasks;

    return toPageResponse(result);
}

        public PageResponse<TaskResponse> getAllTasks(String status, int page, int size, String sortBy, String direction) {
                Sort sort = direction.equalsIgnoreCase("desc")? Sort.by(sortBy).descending(): Sort.by(sortBy).ascending();
                Pageable pageable = PageRequest.of(page, size, sort);
                
                Page<Task> tasks;
                if (status != null && !status.isBlank()) {
                        try {
                                tasks = taskRepository.findByStatus(Task.Status.valueOf(status.toUpperCase()), pageable);
                        } catch (IllegalArgumentException e) {
                                throw new InvalidRequestException("Invalid status: " + status +". Must be PENDING, IN_PROGRESS, or COMPLETED");
                        }
                } else {
                        tasks = taskRepository.findAll(pageable);
                }
                return toPageResponse(tasks);
        }


    public TaskResponse updateTask(Long id, TaskRequest request) {
       Task task = taskRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        if (request.getPriority() != null) {
            task.setPriority(Task.Priority.valueOf(
                    request.getPriority().toUpperCase()));
        }
        task.setDeadline(request.getDeadline());

        if (request.getAssignedToId() != null) {
            User assignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            task.setAssignedTo(assignee);
        }

        Task saved = taskRepository.save(task);
        auditService.log("UPDATE", "TASK", saved.getId(),
                "Task '" + saved.getTitle() + "' updated");
        return toResponse(saved);
    }

    public TaskResponse updateStatus(Long id, String status) {
       Task task = taskRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Task", id));
        task.setStatus(Task.Status.valueOf(status.toUpperCase()));

        Task saved = taskRepository.save(task);
        auditService.log("UPDATE", "TASK", saved.getId(),
                "Task '" + saved.getTitle() + "' status changed to " + status);
        return toResponse(saved);
    }

    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
                throw new ResourceNotFoundException("Task", id);
        }
        auditService.log("DELETE", "TASK", id,
                "Task with id " + id + " deleted");
        taskRepository.deleteById(id);
    }

    private TaskResponse toResponse(Task task) {
    TaskResponse r = new TaskResponse();
    r.setId(task.getId());
    r.setTitle(task.getTitle());
    r.setDescription(task.getDescription());
    r.setStatus(task.getStatus() != null ? task.getStatus().name() : null);
    r.setPriority(task.getPriority() != null ? task.getPriority().name() : null);
    r.setDeadline(task.getDeadline());
    r.setCreatedAt(task.getCreatedAt());
    r.setCreatedByName(task.getCreatedBy().getName());
    r.setAssignedToName(
            task.getAssignedTo() != null ? task.getAssignedTo().getName() : null);

    // Calculate overdue status
    if (task.getDeadline() != null) {
        long days = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.now(), task.getDeadline());
        r.setDaysUntilDeadline(days);

        // Overdue = deadline passed + not completed
        boolean isOverdue = task.getDeadline().isBefore(LocalDate.now())
                && task.getStatus() != Task.Status.COMPLETED;
        r.setOverdue(isOverdue);
    }
        // Auto-escalate priority for tasks due within 2 days
        if (task.getDeadline() != null
                && task.getStatus() != Task.Status.COMPLETED) {
        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.now(), task.getDeadline());
        if (daysLeft <= 2 && daysLeft >= 0) {
                r.setPriority("HIGH");  // escalate in response — DB unchanged
        }
        }

    return r;
}
    private PageResponse<TaskResponse> toPageResponse(Page<Task> page) {
    List<TaskResponse> content = page.getContent()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());

    return new PageResponse<>(
            content,
            page.getNumber(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.hasNext(),
            page.hasPrevious(),
            page.getSize()
    );
}
        // Get all overdue tasks — admin use
public PageResponse<TaskResponse> getOverdueTasks(int page, int size) {
    Pageable pageable = PageRequest.of(page, size,
            Sort.by("deadline").ascending());

    Page<Task> overdueTasks = taskRepository.findOverdueTasks(
            LocalDate.now(), pageable);

    return toPageResponse(overdueTasks);
}

// Get overdue tasks for current user only
public List<TaskResponse> getMyOverdueTasks() {
    User currentUser = getCurrentUser();

    return taskRepository.findOverdueTasks(LocalDate.now())
            .stream()
            .filter(task -> task.getAssignedTo() != null
                    && task.getAssignedTo().getId().equals(currentUser.getId()))
            .map(this::toResponse)
            .collect(Collectors.toList());
}

public TaskStatsResponse getTaskStats() {
    long total = taskRepository.count();
    long pending = taskRepository.countByStatus(Task.Status.PENDING);
    long inProgress = taskRepository.countByStatus(Task.Status.IN_PROGRESS);
    long completed = taskRepository.countByStatus(Task.Status.COMPLETED);
    long overdue = taskRepository.countOverdueTasks(LocalDate.now());

    double completionRate = total > 0
            ? Math.round((completed * 100.0 / total) * 10.0) / 10.0
            : 0.0;

    return TaskStatsResponse.builder()
            .totalTasks(total)
            .pendingTasks(pending)
            .inProgressTasks(inProgress)
            .completedTasks(completed)
            .overdueTasks(overdue)
            .completionRate(completionRate)
            .build();
}

// Admin search across all tasks
public PageResponse<TaskResponse> searchTasks(String keyword,
        int page, int size) {
    if (keyword == null || keyword.isBlank()) {
        throw new InvalidRequestException("Search keyword cannot be empty");
    }

    Pageable pageable = PageRequest.of(page, size,
            Sort.by("createdAt").descending());

    Page<Task> results = taskRepository.searchTasks(
            keyword.trim(), pageable);

    return toPageResponse(results);
}

// User search within their own tasks
public PageResponse<TaskResponse> searchMyTasks(String keyword,
        int page, int size) {
    if (keyword == null || keyword.isBlank()) {
        throw new InvalidRequestException("Search keyword cannot be empty");
    }

    User currentUser = getCurrentUser();
    Pageable pageable = PageRequest.of(page, size,
            Sort.by("createdAt").descending());

    Page<Task> results = taskRepository.searchMyTasks(
            keyword.trim(), currentUser, pageable);

    return toPageResponse(results);
}

    List<TaskResponse> getMyTasks() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // Admin — all tasks due within next N days
public PageResponse<TaskResponse> getUpcomingTasks(int days,
        int page, int size) {
    if (days < 1 || days > 30) {
        throw new InvalidRequestException(
                "Days must be between 1 and 30");
    }

    LocalDate today = LocalDate.now();
    LocalDate futureDate = today.plusDays(days);
    Pageable pageable = PageRequest.of(page, size,
            Sort.by("deadline").ascending());

    Page<Task> tasks = taskRepository.findTasksDueBetween(
            today, futureDate, pageable);

    return toPageResponse(tasks);
}

// User — their own tasks due within next N days
public PageResponse<TaskResponse> getMyUpcomingTasks(int days,
        int page, int size) {
    if (days < 1 || days > 30) {
        throw new InvalidRequestException(
                "Days must be between 1 and 30");
    }

    User currentUser = getCurrentUser();
    LocalDate today = LocalDate.now();
    LocalDate futureDate = today.plusDays(days);
    Pageable pageable = PageRequest.of(page, size,
            Sort.by("deadline").ascending());

    Page<Task> tasks = taskRepository.findMyTasksDueBetween(
            today, futureDate, currentUser, pageable);

    return toPageResponse(tasks);
}

public UserStatsResponse getMyStats() {
    User currentUser = getCurrentUser();

    long total = taskRepository.countByAssignedTo(currentUser);
    long completed = taskRepository.countByAssignedToAndStatus(
            currentUser, Task.Status.COMPLETED);
    long pending = taskRepository.countByAssignedToAndStatus(
            currentUser, Task.Status.PENDING);
    long inProgress = taskRepository.countByAssignedToAndStatus(
            currentUser, Task.Status.IN_PROGRESS);
    long overdue = taskRepository.countOverdueTasksForUser(
            currentUser, LocalDate.now());

    double completionRate = total > 0
            ? Math.round((completed * 100.0 / total) * 10.0) / 10.0
            : 0.0;

    return UserStatsResponse.builder()
            .userName(currentUser.getName())
            .email(currentUser.getEmail())
            .totalAssigned(total)
            .completed(completed)
            .pending(pending)
            .inProgress(inProgress)
            .overdue(overdue)
            .completionRate(completionRate)
            .build();
}
private final AuditLogRepository auditLogRepository;
public List<AuditLog> getTaskActivity(Long taskId) {
    // Verify task exists first
    if (!taskRepository.existsById(taskId)) {
        throw new ResourceNotFoundException("Task", taskId);
    }

    return auditLogRepository.findByEntityTypeAndEntityIdOrderByPerformedAtDesc(
                    "TASK", taskId);
}
}