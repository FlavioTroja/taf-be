package it.overzoom.taf.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import it.overzoom.taf.model.Notification;

public interface NotificationService {

    Page<Notification> findAll(Pageable pageable);

    Optional<Notification> findById(String id);

    boolean existsById(String id);

    Notification create(Notification notification);

    Optional<Notification> update(Notification notification);

    Optional<Notification> partialUpdate(String id, Notification notification);

    void deleteById(String id);

    void sendPushToUser(String userId, String title, String body, Map<String, String> data);
}
