package it.overzoom.taf.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.model.Event;
import it.overzoom.taf.model.User;
import it.overzoom.taf.repository.EventRepository;
import it.overzoom.taf.repository.UserRepository;
import it.overzoom.taf.type.EntityType;
import it.overzoom.taf.type.NotificationType;
import it.overzoom.taf.type.PhotoType;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final PhotoService photoService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EventServiceImpl.class);

    public EventServiceImpl(EventRepository eventRepository, PhotoService photoService,
            NotificationService notificationService, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.photoService = photoService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @Override
    public Page<Event> findAll(Pageable pageable) {
        log.info("Fetching all events with pagination: {}", pageable);
        return eventRepository.findAll(pageable);
    }

    @Override
    public Optional<Event> findById(String id) {
        log.info("Fetching event by ID: {}", id);
        return eventRepository.findById(id);
    }

    @Override
    public boolean existsById(String id) {
        log.info("Checking if event exists by ID: {}", id);
        return eventRepository.existsById(id);
    }

    @Override
    @Transactional
    public Event create(Event event) {
        log.info("Creating new event: {}", event);
        event = eventRepository.save(event);

        // Notifica push solo agli utenti iscritti il cui municipalityId dell'evento è
        // contenuto nei municipalityIds dell'utente
        List<User> subscribedUsers = userRepository.findByNotificationTypesContaining(NotificationType.NEW_EVENTS);
        for (User user : subscribedUsers) {
            if (Arrays.asList(user.getMunicipalityIds()).contains(event.getMunicipalityId())) {
                log.info("Sending push notification to user {} for new event: {}", user.getId(), event.getTitle());
                notificationService.sendPushToUser(user.getId(), "Nuovo evento", event.getTitle(),
                        Map.of("eventId", event.getId(), "type", NotificationType.NEW_EVENTS.name()));
            }
        }

        return event;
    }

    @Override
    @Transactional
    public Optional<Event> update(Event event) {
        log.info("Updating event with ID: {}", event.getId());
        return eventRepository.findById(event.getId()).map(existing -> {
            existing.setTitle(event.getTitle());
            existing.setDescription(event.getDescription());
            existing.setType(event.getType());
            existing.setStartDateTime(event.getStartDateTime());
            existing.setEndDateTime(event.getEndDateTime());
            existing.setLocation(event.getLocation());
            existing.setPhotos(event.getPhotos());
            existing.setOrganizer(event.getOrganizer());
            existing.setContactEmail(event.getContactEmail());
            existing.setContactPhone(event.getContactPhone());
            existing.setTags(event.getTags());
            existing.setMunicipalityId(event.getMunicipalityId());
            existing.setActivityId(event.getActivityId());
            existing.setMaxParticipants(event.getMaxParticipants());
            existing.setCurrentParticipants(event.getCurrentParticipants());
            existing.setIsPublic(event.getIsPublic());
            existing.setIsCancelled(event.getIsCancelled());
            existing.setUrl(event.getUrl());

            // Notifica tutti i partecipanti che l'evento è stato aggiornato
            for (String userId : existing.getParticipants()) {
                Optional<User> userOpt = userRepository.findById(userId);
                userOpt.ifPresent(user -> {
                    if (Arrays.asList(user.getNotificationTypes()).contains(NotificationType.SUBSCRIPTION_EVENTS)) {
                        log.info("Sending push notification to user {} for updated event: {}", user.getId(),
                                existing.getTitle());
                        String title = "L'evento " + existing.getTitle() + " è stato aggiornato!";
                        String message = "Dettagli dell'evento: " + existing.getTitle();
                        notificationService.sendPushToUser(user.getId(), title, message,
                                Map.of("eventId", existing.getId(), "type",
                                        NotificationType.SUBSCRIPTION_EVENTS.name()));
                    }
                });
            }

            return existing;
        }).map(eventRepository::save);
    }

    @Override
    @Transactional
    public Optional<Event> partialUpdate(String id, Event event) {
        log.info("Partially updating event with ID: {}", id);
        return eventRepository.findById(id).map(existing -> {
            boolean isUpdated = false;

            if (event.getTitle() != null) {
                existing.setTitle(event.getTitle());
                isUpdated = true;
            }
            if (event.getDescription() != null) {
                existing.setDescription(event.getDescription());
                isUpdated = true;
            }
            if (event.getType() != null) {
                existing.setType(event.getType());
                isUpdated = true;
            }
            if (event.getStartDateTime() != null) {
                existing.setStartDateTime(event.getStartDateTime());
                isUpdated = true;
            }
            if (event.getEndDateTime() != null) {
                existing.setEndDateTime(event.getEndDateTime());
                isUpdated = true;
            }
            if (event.getLocation() != null) {
                existing.setLocation(event.getLocation());
                isUpdated = true;
            }
            if (event.getPhotos() != null) {
                existing.setPhotos(event.getPhotos());
                isUpdated = true;
            }
            if (event.getOrganizer() != null) {
                existing.setOrganizer(event.getOrganizer());
                isUpdated = true;
            }
            if (event.getContactEmail() != null) {
                existing.setContactEmail(event.getContactEmail());
                isUpdated = true;
            }
            if (event.getContactPhone() != null) {
                existing.setContactPhone(event.getContactPhone());
                isUpdated = true;
            }
            if (event.getTags() != null) {
                existing.setTags(event.getTags());
                isUpdated = true;
            }
            if (event.getMunicipalityId() != null) {
                existing.setMunicipalityId(event.getMunicipalityId());
                isUpdated = true;
            }
            if (event.getActivityId() != null) {
                existing.setActivityId(event.getActivityId());
                isUpdated = true;
            }
            if (event.getMaxParticipants() != null) {
                existing.setMaxParticipants(event.getMaxParticipants());
                isUpdated = true;
            }
            if (event.getCurrentParticipants() != null) {
                existing.setCurrentParticipants(event.getCurrentParticipants());
                isUpdated = true;
            }
            if (event.getIsPublic() != null) {
                existing.setIsPublic(event.getIsPublic());
                isUpdated = true;
            }
            if (event.getIsCancelled() != null) {
                existing.setIsCancelled(event.getIsCancelled());
                isUpdated = true;
            }
            if (event.getUrl() != null) {
                existing.setUrl(event.getUrl());
                isUpdated = true;
            }

            // Se l'evento è stato aggiornato, invia notifiche ai partecipanti
            if (isUpdated) {
                for (String userId : existing.getParticipants()) {
                    Optional<User> userOpt = userRepository.findById(userId);
                    userOpt.ifPresent(user -> {
                        if (Arrays.asList(user.getNotificationTypes()).contains(NotificationType.SUBSCRIPTION_EVENTS)) {
                            log.info("Sending push notification to user {} for updated event: {}", user.getId(),
                                    existing.getTitle());
                            String title = "L'evento " + existing.getTitle() + " è stato aggiornato!";
                            String message = "Dettagli dell'evento: " + existing.getTitle();
                            notificationService.sendPushToUser(user.getId(), title, message,
                                    Map.of("eventId", existing.getId(), "type",
                                            NotificationType.SUBSCRIPTION_EVENTS.name()));
                        }
                    });
                }
            }

            return existing;
        }).map(eventRepository::save);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        log.info("Deleting event with ID: {}", id);
        eventRepository.deleteById(id);
    }

    @Transactional
    public Event uploadCover(String eventId, MultipartFile file) throws IOException, ResourceNotFoundException {
        log.info("Uploading cover for event with ID: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato con ID: " + eventId));

        String path = photoService.uploadPhoto(EntityType.EVENT, eventId, file, PhotoType.COVER);
        event.setCover(path);
        eventRepository.save(event);
        return event;
    }

    @Transactional
    public Event uploadGallery(String eventId, MultipartFile[] files)
            throws IOException, ResourceNotFoundException {
        log.info("Uploading gallery for event with ID: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato con ID: " + eventId));

        List<String> photos = new ArrayList<>(
                event.getPhotos() != null ? Arrays.asList(event.getPhotos()) : List.of());

        // Trova il prossimo progressivo disponibile
        int nextIndex = 1;
        Pattern pattern = Pattern.compile("gallery_" + eventId + "_(\\d+)\\.[a-z]+$");
        for (String photo : photos) {
            Matcher matcher = pattern.matcher(photo);
            if (matcher.find()) {
                int idx = Integer.parseInt(matcher.group(1));
                if (idx >= nextIndex)
                    nextIndex = idx + 1;
            }
        }

        for (MultipartFile file : files) {
            String path = photoService.uploadPhoto(EntityType.EVENT, eventId, file, PhotoType.GALLERY, nextIndex);
            photos.add(path);
            nextIndex++;
        }

        event.setPhotos(photos.toArray(new String[0]));
        eventRepository.save(event);
        return event;
    }

    @Transactional
    public Event deleteGallery(String eventId, String photoName) throws IOException, ResourceNotFoundException {
        log.info("Deleting gallery photo {} for event with ID: {}", photoName, eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato con ID: " + eventId));
        String[] currentPhotos = event.getPhotos() != null ? event.getPhotos() : new String[0];
        List<String> photos = new ArrayList<>(Arrays.asList(currentPhotos));
        boolean removed = photos.removeIf(p -> p.endsWith(photoName)); // o usa equals se salvi solo il nome

        if (removed) {
            String uploadPath = photoService.getBaseUploadPath();
            photoService.deletePhoto(uploadPath + File.separator + EntityType.EVENT.name().toLowerCase()
                    + File.separator + eventId + File.separator + photoName);
            event.setPhotos(photos.toArray(new String[0]));
            eventRepository.save(event);
        }
        return event;
    }

    public boolean canUserRegister(String eventId) {
        log.info("Checking if user can register for event with ID: {}", eventId);
        Optional<Event> event = findById(eventId);
        if (event.isPresent()) {
            Event e = event.get();

            if (e.getMaxParticipants() == null) {
                return true;
            }

            if (e.getCurrentParticipants() >= e.getMaxParticipants()) {
                if (!e.getIsPublic()) {
                    return false;
                }

                if (e.getCurrentParticipants() + 1 > e.getMaxParticipants() + e.getMaxParticipants() * 0.1) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isUserRegistered(String eventId, String userId) {
        log.info("Checking if user with ID: {} is registered for event with ID: {}", userId, eventId);
        Optional<Event> event = findById(eventId);
        return event.map(value -> value.getParticipants().contains(userId)).orElse(false);
    }

    @Transactional
    @Override
    public void registerUserToEvent(String eventId, String userId) throws ResourceNotFoundException,
            BadRequestException {
        log.info("Registering user with ID: {} to event with ID: {}", userId, eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato con ID: " + eventId));

        if (event.getIsCancelled()) {
            throw new BadRequestException("L'evento è stato cancellato.");
        }

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con ID: " + userId));

        if (event.getParticipants().contains(user.getId())) {
            throw new BadRequestException("L'utente è già registrato a questo evento.");
        }

        if (!canUserRegister(eventId)) {
            throw new BadRequestException("L'evento è pieno.");
        }

        event.addParticipant(user.getId());

        eventRepository.save(event);
        notificationService.sendPushToUser(
                user.getId(),
                "Registrazione evento",
                "La registrazione all'evento '" + event.getTitle() + "' è avvenuta con successo!",
                Map.of("eventId", eventId));

    }

    @Transactional
    @Override
    public void unregisterUserFromEvent(String eventId, String userId) throws ResourceNotFoundException,
            BadRequestException {
        log.info("Unregistering user with ID: {} from event with ID: {}", userId, eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato con ID: " + eventId));

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con ID: " + userId));

        if (!event.getParticipants().contains(user.getId())) {
            throw new BadRequestException("L'utente non è registrato a questo evento.");
        }

        event.removeParticipant(user.getId());

        if (event.getCheckInTimes().containsKey(user.getId())) {
            event.getCheckInTimes().remove(user.getId());
        }

        eventRepository.save(event);
        notificationService.sendPushToUser(
                user.getId(),
                "Cancellazione registrazione evento",
                "La registrazione all'evento '" + event.getTitle() + "' è stata annullata.",
                Map.of("eventId", eventId));
    }

    @Transactional
    @Override
    public void checkInUser(String eventId, String userId) throws ResourceNotFoundException, BadRequestException {
        log.info("Checking in user with ID: {} for event with ID: {}", userId, eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato con ID: " + eventId));

        if (!event.getParticipants().contains(userId)) {
            throw new BadRequestException("L'utente non è registrato a questo evento.");
        }

        event.addCheckIn(userId);

        eventRepository.save(event);
    }

    public Page<Event> getEventsByUserId(String userId, Pageable pageable) {
        log.info("Fetching events for user with ID: {}", userId);
        return eventRepository.findEventsByUserId(userId, pageable);
    }
}
