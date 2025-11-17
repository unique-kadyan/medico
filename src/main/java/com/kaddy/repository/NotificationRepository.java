package com.kaddy.repository;

import com.kaddy.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdAndReadOrderBySentAtDesc(Long userId, Boolean read);

    List<Notification> findByUserIdOrderBySentAtDesc(Long userId);

    Long countByUserIdAndReadFalse(Long userId);
}
