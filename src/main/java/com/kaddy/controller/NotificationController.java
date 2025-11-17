package com.kaddy.controller;

import com.kaddy.dto.NotificationDTO;
import com.kaddy.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "APIs for managing user notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Retrieve a specific notification by its ID")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @GetMapping("/user/{id}")
    @Operation(summary = "Get all notifications for user", description = "Retrieve all notifications for a specific user")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByUser(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationsByUser(id));
    }

    @GetMapping("/user/{id}/unread")
    @Operation(summary = "Get unread notifications", description = "Retrieve all unread notifications for a specific user")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(id));
    }

    @GetMapping("/user/{id}/count")
    @Operation(summary = "Get unread notification count", description = "Get the count of unread notifications for a specific user")
    public ResponseEntity<Map<String, Long>> getUnreadNotificationCount(@PathVariable Long id) {
        Long count = notificationService.getUnreadNotificationCount(id);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PostMapping
    @Operation(summary = "Create a notification", description = "Create a new notification for a user")
    public ResponseEntity<NotificationDTO> createNotification(@Valid @RequestBody NotificationDTO notificationDTO) {
        NotificationDTO createdNotification = notificationService.createNotification(notificationDTO);
        return new ResponseEntity<>(createdNotification, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/user/{id}/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Mark all notifications as read for a specific user")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long id) {
        notificationService.markAllAsRead(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification", description = "Delete a specific notification")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
