package it.overzoom.taf.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.model.Event;

public interface EventService {

    Page<Event> findAll(Pageable pageable);

    Optional<Event> findById(String id);

    boolean existsById(String id);

    Event create(Event event);

    Optional<Event> update(Event event);

    Optional<Event> partialUpdate(String id, Event event);

    void deleteById(String id);

    Event uploadCover(String eventId, MultipartFile file) throws IOException, ResourceNotFoundException;

    Event uploadGallery(String eventId, MultipartFile[] files) throws IOException, ResourceNotFoundException;

    Event deleteGallery(String eventId, String photoName) throws IOException, ResourceNotFoundException;
}
