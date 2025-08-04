package it.overzoom.taf.service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.overzoom.taf.model.Notification;
import it.overzoom.taf.repository.NotificationRepository;
import it.overzoom.taf.repository.UserRepository;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FcmNotificationService fcmNotificationService;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository,
            FcmNotificationService fcmNotificationService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.fcmNotificationService = fcmNotificationService;
    }

    @Override
    public Page<Notification> findAll(Pageable pageable) {
        return notificationRepository.findAll(pageable);
    }

    @Override
    public Optional<Notification> findById(String id) {
        return notificationRepository.findById(id);
    }

    @Override
    public boolean existsById(String id) {
        return notificationRepository.existsById(id);
    }

    @Override
    @Transactional
    public Notification create(Notification notification) {
        if (notification.getSenderId() == null) {

        }
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public Optional<Notification> update(Notification notification) {
        return notificationRepository.findById(notification.getId()).map(existing -> {
            existing.setMessage(notification.getMessage());
            existing.setRecipientId(notification.getRecipientId());
            existing.setSenderId(notification.getSenderId());
            existing.setRead(notification.isRead());
            existing.setTimestamp(notification.getTimestamp());
            existing.setMunicipalityId(notification.getMunicipalityId());
            return existing;
        }).map(notificationRepository::save);
    }

    @Override
    @Transactional
    public Optional<Notification> partialUpdate(String id, Notification notification) {
        return notificationRepository.findById(id).map(existing -> {
            if (notification.getMessage() != null)
                existing.setMessage(notification.getMessage());
            if (notification.getRecipientId() != null)
                existing.setRecipientId(notification.getRecipientId());
            if (notification.getSenderId() != null)
                existing.setSenderId(notification.getSenderId());
            existing.setRead(notification.isRead()); // boolean defaults to false if not set
            if (notification.getTimestamp() != 0L)
                existing.setTimestamp(notification.getTimestamp());
            if (notification.getMunicipalityId() != null)
                existing.setMunicipalityId(notification.getMunicipalityId());
            return existing;
        }).map(notificationRepository::save);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        notificationRepository.deleteById(id);
    }

    @Override
    public void sendPushToUser(String userId, String title, String body, Map<String, String> data) {
        userRepository.findById(userId).ifPresent(user -> {
            String fcmToken = user.getFcmToken();
            if (fcmToken == null || fcmToken.isEmpty())
                return;

            try {
                boolean sent = fcmNotificationService.sendNotification(fcmToken, title, body, data);
                // puoi fare anche logging/statistiche qui!
                if (!sent) {
                    log.warn("Invio FCM fallito per utente {} (token: {})", userId, fcmToken);
                }
            } catch (IOException ex) {
                user.setFcmToken(null);
                userRepository.save(user);
                log.warn("FcmToken rimosso per user {}: {}", userId, ex.getMessage());
            } catch (Exception ex) {
                log.error("Errore invio notifica push a {}: {}", userId, ex.getMessage(), ex);
            }
        });
    }

}
