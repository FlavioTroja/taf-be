package it.overzoom.taf.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import it.overzoom.taf.repository.EventRepository;
import it.overzoom.taf.type.EntityType;
import it.overzoom.taf.type.PhotoType;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final PhotoService photoService;

    public EventServiceImpl(EventRepository eventRepository, PhotoService photoService) {
        this.eventRepository = eventRepository;
        this.photoService = photoService;
    }

    @Override
    public Page<Event> findAll(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    @Override
    public Optional<Event> findById(String id) {
        return eventRepository.findById(id);
    }

    @Override
    public boolean existsById(String id) {
        return eventRepository.existsById(id);
    }

    @Override
    @Transactional
    public Event create(Event event) {
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public Optional<Event> update(Event event) {
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
            return existing;
        }).map(eventRepository::save);
    }

    @Override
    @Transactional
    public Optional<Event> partialUpdate(String id, Event event) {
        return eventRepository.findById(id).map(existing -> {
            if (event.getTitle() != null)
                existing.setTitle(event.getTitle());
            if (event.getDescription() != null)
                existing.setDescription(event.getDescription());
            if (event.getType() != null)
                existing.setType(event.getType());
            if (event.getStartDateTime() != null)
                existing.setStartDateTime(event.getStartDateTime());
            if (event.getEndDateTime() != null)
                existing.setEndDateTime(event.getEndDateTime());
            if (event.getLocation() != null)
                existing.setLocation(event.getLocation());
            if (event.getPhotos() != null)
                existing.setPhotos(event.getPhotos());
            if (event.getOrganizer() != null)
                existing.setOrganizer(event.getOrganizer());
            if (event.getContactEmail() != null)
                existing.setContactEmail(event.getContactEmail());
            if (event.getContactPhone() != null)
                existing.setContactPhone(event.getContactPhone());
            if (event.getTags() != null)
                existing.setTags(event.getTags());
            if (event.getMunicipalityId() != null)
                existing.setMunicipalityId(event.getMunicipalityId());
            if (event.getActivityId() != null)
                existing.setActivityId(event.getActivityId());
            if (event.getMaxParticipants() != null)
                existing.setMaxParticipants(event.getMaxParticipants());
            if (event.getCurrentParticipants() != null)
                existing.setCurrentParticipants(event.getCurrentParticipants());
            if (event.getIsPublic() != null)
                existing.setIsPublic(event.getIsPublic());
            if (event.getIsCancelled() != null)
                existing.setIsCancelled(event.getIsCancelled());
            if (event.getUrl() != null)
                existing.setUrl(event.getUrl());
            return existing;
        }).map(eventRepository::save);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        eventRepository.deleteById(id);
    }

    @Transactional
    public Event uploadCover(String eventId, MultipartFile file) throws IOException, ResourceNotFoundException {
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
        Optional<Event> event = findById(eventId);
        if (event.isPresent()) {
            Event e = event.get();

            // Se maxParticipants è null, non ci sono limiti di partecipanti
            if (e.getMaxParticipants() == null) {
                return true;
            }

            // Se l'evento ha un limite di partecipanti, verifica se è stato superato
            if (e.getCurrentParticipants() >= e.getMaxParticipants()) {
                // Controllo per overbooking
                if (!e.getIsPublic()) {
                    return false; // Se non è pubblico, non si può superare il limite
                }

                // Implementa la logica per overbooking, ad esempio:
                if (e.getCurrentParticipants() + 1 > e.getMaxParticipants() + e.getMaxParticipants() * 0.1) {
                    return false; // Se l'overbooking supera il 10% del limite, impedisci l'iscrizione
                }
            }
        }
        return true;
    }

    public boolean isUserRegistered(String eventId, String userId) {
        Optional<Event> event = findById(eventId);
        return event.map(value -> value.getParticipants().contains(userId)).orElse(false);
    }

    public void registerUserToEvent(String eventId, String userId) throws ResourceNotFoundException,
            BadRequestException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato con ID: " + eventId));

        // Verifica se l'evento è già cancellato
        if (event.getIsCancelled()) {
            throw new BadRequestException("L'evento è stato cancellato.");
        }

        // Verifica se l'utente è già registrato
        if (event.getParticipants().contains(userId)) {
            throw new BadRequestException("L'utente è già registrato a questo evento.");
        }

        // Verifica se l'evento è pieno
        if (!canUserRegister(eventId)) {
            throw new BadRequestException("L'evento è pieno.");
        }

        // Aggiungi il partecipante
        event.addParticipant(userId);

        // Salva il partecipante e aggiorna il numero di partecipanti
        eventRepository.save(event);
    }

    public void unregisterUserFromEvent(String eventId, String userId) throws ResourceNotFoundException,
            BadRequestException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato con ID: " + eventId));

        // Verifica se l'utente è iscritto
        if (!event.getParticipants().contains(userId)) {
            throw new BadRequestException("L'utente non è registrato a questo evento.");
        }

        // Rimuovi il partecipante
        event.removeParticipant(userId);

        // Rimuovi anche il check-in dell'utente dalla mappa
        if (event.getCheckInTimes().containsKey(userId)) {
            event.getCheckInTimes().remove(userId);
        }

        // Salva l'evento aggiornato
        eventRepository.save(event);
    }

    public void checkInUser(String eventId, String userId) throws ResourceNotFoundException, BadRequestException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato con ID: " + eventId));

        // Verifica se l'utente è registrato
        if (!event.getParticipants().contains(userId)) {
            throw new BadRequestException("L'utente non è registrato a questo evento.");
        }

        // Aggiungi il check-in time
        event.addCheckIn(userId);

        // Salva l'evento con il nuovo check-in
        eventRepository.save(event);
    }

    public Page<Event> getEventsByUserId(String userId, Pageable pageable) {
        return eventRepository.findEventsByUserId(userId, pageable);
    }
}
