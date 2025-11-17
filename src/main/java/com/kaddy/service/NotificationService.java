package com.kaddy.service;

import com.kaddy.dto.NotificationDTO;
import com.kaddy.exception.ResourceNotFoundException;
import com.kaddy.model.Notification;
import com.kaddy.model.User;
import com.kaddy.repository.NotificationRepository;
import com.kaddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public NotificationDTO getNotificationById(Long id) {
        log.info("Fetching notification with ID: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));
        return convertToDTO(notification);
    }

    public List<NotificationDTO> getNotificationsByUser(Long userId) {
        log.info("Fetching all notifications for user ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        log.info("Fetching unread notifications for user ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        return notificationRepository.findByUserIdAndReadOrderBySentAtDesc(userId, false)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Long getUnreadNotificationCount(Long userId) {
        log.info("Counting unread notifications for user ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    public NotificationDTO createNotification(NotificationDTO notificationDTO) {
        log.info("Creating notification for user ID: {}", notificationDTO.getUserId());

        User user = userRepository.findById(notificationDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + notificationDTO.getUserId()));

        Notification notification = convertToEntity(notificationDTO);
        notification.setUser(user);
        notification.setRead(false);
        notification.setSentAt(LocalDateTime.now());

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created successfully with ID: {}", savedNotification.getId());

        return convertToDTO(savedNotification);
    }

    public NotificationDTO createNotification(Long userId, String title, String message,
                                            Notification.NotificationType type, String referenceType, Long referenceId) {
        log.info("Creating notification for user ID: {} with type: {}", userId, type);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setSentAt(LocalDateTime.now());
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created successfully with ID: {}", savedNotification.getId());

        return convertToDTO(savedNotification);
    }

    public NotificationDTO markAsRead(Long id) {
        log.info("Marking notification as read with ID: {}", id);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());

        Notification updatedNotification = notificationRepository.save(notification);
        log.info("Notification marked as read with ID: {}", updatedNotification.getId());

        return convertToDTO(updatedNotification);
    }

    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user ID: {}", userId);

        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndReadOrderBySentAtDesc(userId, false);

        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        });

        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked {} notifications as read for user ID: {}", unreadNotifications.size(), userId);
    }

    public void deleteNotification(Long id) {
        log.info("Deleting notification with ID: {}", id);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));

        notificationRepository.delete(notification);
        log.info("Notification deleted successfully with ID: {}", id);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = modelMapper.map(notification, NotificationDTO.class);
        dto.setUserId(notification.getUser().getId());
        return dto;
    }

    private Notification convertToEntity(NotificationDTO dto) {
        return modelMapper.map(dto, Notification.class);
    }
}
