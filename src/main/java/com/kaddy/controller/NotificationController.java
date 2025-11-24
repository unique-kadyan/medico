package com.kaddy.controller;

import com.kaddy.dto.NotificationDTO;
import com.kaddy.service.NotificationService;
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
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByUser(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationsByUser(id));
    }

    @GetMapping("/user/{id}/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(id));
    }

    @GetMapping("/user/{id}/count")
    public ResponseEntity<Map<String, Long>> getUnreadNotificationCount(@PathVariable Long id) {
        Long count = notificationService.getUnreadNotificationCount(id);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PostMapping
    public ResponseEntity<NotificationDTO> createNotification(@Valid @RequestBody NotificationDTO notificationDTO) {
        NotificationDTO createdNotification = notificationService.createNotification(notificationDTO);
        return new ResponseEntity<>(createdNotification, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationDTO> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/user/{id}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long id) {
        notificationService.markAllAsRead(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
