package it.overzoom.taf.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Function;

import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.overzoom.taf.dto.EventDTO;
import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.mapper.EventMapper;
import it.overzoom.taf.model.Event;
import it.overzoom.taf.service.EventService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/events")
public class EventController extends BaseSearchController<Event, EventDTO> {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);
    private final EventService eventService;
    private final EventMapper eventMapper;

    public EventController(EventService eventService, EventMapper eventMapper) {
        this.eventService = eventService;
        this.eventMapper = eventMapper;
    }

    @Override
    protected String getCollectionName() {
        return "event";
    }

    @Override
    protected Class<Event> getEntityClass() {
        return Event.class;
    }

    @Override
    protected Function<Event, EventDTO> toDtoMapper() {
        return eventMapper::toDto;
    }

    @Override
    protected List<String> getSearchableFields() {
        return List.of(
                "title", "description", "tags",
                "municipalityId", "activityId");
    }

    @GetMapping("")
    public ResponseEntity<Page<EventDTO>> findAll(Pageable pageable) {
        log.info("REST request to get a page of Events");
        Page<Event> page = eventService.findAll(pageable);
        return ResponseEntity.ok().body(page.map(eventMapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> findById(@PathVariable("id") String id) throws ResourceNotFoundException {
        return eventService.findById(id)
                .map(eventMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato."));
    }

    @PostMapping("/create")
    public ResponseEntity<EventDTO> create(@Valid @RequestBody EventDTO eventDTO)
            throws BadRequestException, URISyntaxException {
        log.info("REST request to save Event : {}", eventDTO);
        if (eventDTO.getId() != null) {
            throw new BadRequestException("Un nuovo evento non può già avere un ID");
        }
        Event event = eventMapper.toEntity(eventDTO);
        event = eventService.create(event);
        return ResponseEntity.created(new URI("/api/events/" + event.getId())).body(eventMapper.toDto(event));
    }

    @PutMapping("")
    public ResponseEntity<EventDTO> update(@Valid @RequestBody EventDTO eventDTO)
            throws BadRequestException, ResourceNotFoundException {
        log.info("REST request to update Event: {}", eventDTO);
        if (eventDTO.getId() == null) {
            throw new BadRequestException("ID invalido.");
        }
        if (!eventService.existsById(eventDTO.getId())) {
            throw new ResourceNotFoundException("Evento non trovato.");
        }
        Event event = eventMapper.toEntity(eventDTO);
        Event updated = eventService.update(event)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evento non trovato con questo ID :: " + event.getId()));

        return ResponseEntity.ok().body(eventMapper.toDto(updated));
    }

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<EventDTO> partialUpdate(@PathVariable("id") String id,
            @RequestBody EventDTO eventDTO)
            throws BadRequestException, ResourceNotFoundException {
        log.info("REST request to partial update Event: {}", eventDTO);
        if (id == null) {
            throw new BadRequestException("ID invalido.");
        }
        if (!eventService.existsById(id)) {
            throw new ResourceNotFoundException("Evento non trovato.");
        }
        Event event = eventMapper.toEntity(eventDTO);
        Event updated = eventService.partialUpdate(id, event)
                .orElseThrow(() -> new ResourceNotFoundException("Evento non trovato con questo ID :: " + id));

        return ResponseEntity.ok().body(eventMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") String id) throws ResourceNotFoundException {
        log.info("REST request to delete Event with ID: {}", id);
        if (!eventService.existsById(id)) {
            throw new ResourceNotFoundException("Evento non trovato.");
        }
        eventService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
