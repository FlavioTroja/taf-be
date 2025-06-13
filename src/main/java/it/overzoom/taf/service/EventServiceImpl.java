package it.overzoom.taf.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.overzoom.taf.model.Event;
import it.overzoom.taf.repository.EventRepository;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
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
}
