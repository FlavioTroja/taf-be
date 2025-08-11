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
        log.info("Recuperando tutte le notifiche con paginazione...");
        return notificationRepository.findAll(pageable);
    }

    @Override
    public Optional<Notification> findById(String id) {
        log.info("Ricerca della notifica con ID: {}", id);
        return notificationRepository.findById(id);
    }

    @Override
    public boolean existsById(String id) {
        log.info("Verifica se la notifica con ID: {} esiste...", id);
        return notificationRepository.existsById(id);
    }

    @Override
    @Transactional
    public Notification create(Notification notification) {
        log.info("Creazione di una nuova notifica...");
        if (notification.getSenderId() == null) {
            log.warn("Sender ID non fornito per la notifica con ID: {}", notification.getId());
        }
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notifica creata con successo, ID: {}", savedNotification.getId());
        return savedNotification;
    }

    @Override
    @Transactional
    public Optional<Notification> update(Notification notification) {
        log.info("Aggiornamento della notifica con ID: {}", notification.getId());
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
        log.info("Aggiornamento parziale della notifica con ID: {}", id);
        return notificationRepository.findById(id).map(existing -> {
            if (notification.getMessage() != null) {
                existing.setMessage(notification.getMessage());
            }
            if (notification.getRecipientId() != null) {
                existing.setRecipientId(notification.getRecipientId());
            }
            if (notification.getSenderId() != null) {
                existing.setSenderId(notification.getSenderId());
            }
            existing.setRead(notification.isRead()); // boolean defaults to false if not set
            if (notification.getTimestamp() != 0L) {
                existing.setTimestamp(notification.getTimestamp());
            }
            if (notification.getMunicipalityId() != null) {
                existing.setMunicipalityId(notification.getMunicipalityId());
            }
            return existing;
        }).map(notificationRepository::save);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        log.info("Cancellazione della notifica con ID: {}", id);
        notificationRepository.deleteById(id);
    }

    @Override
    public void sendPushToUser(String userId, String title, String body, Map<String, String> data) {
        log.info("Invio notifica push all'utente con ID: {}", userId);
        userRepository.findById(userId).ifPresent(user -> {
            String fcmToken = user.getFcmToken();
            if (fcmToken == null || fcmToken.isEmpty()) {
                log.warn("FcmToken non trovato per l'utente con ID: {}", userId);
                return;
            }

            try {
                boolean sent = fcmNotificationService.sendNotification(fcmToken, title, body, data);
                if (!sent) {
                    log.warn("Invio FCM fallito per utente {} (token: {})", userId, fcmToken);
                } else {
                    log.info("Notifica FCM inviata con successo all'utente {}", userId);
                }
            } catch (IOException ex) {
                // user.setFcmToken(null);
                // userRepository.save(user);
                log.warn("FcmToken rimosso per user {} a causa di un errore: {}", userId, ex.getMessage());
            } catch (Exception ex) {
                log.error("Errore durante l'invio della notifica push a {}: {}", userId, ex.getMessage(), ex);
            }
        });
    }
}
