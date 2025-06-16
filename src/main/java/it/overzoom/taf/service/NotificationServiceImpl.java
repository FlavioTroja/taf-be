package it.overzoom.taf.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.overzoom.taf.model.Notification;
import it.overzoom.taf.repository.NotificationRepository;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
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
}
