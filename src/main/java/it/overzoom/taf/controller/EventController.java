package it.overzoom.taf.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @PostMapping("/{id}/upload-cover")
    public ResponseEntity<EventDTO> uploadCover(@PathVariable("id") String id,
            @RequestParam("file") MultipartFile file)
            throws ResourceNotFoundException, IOException {
        Event event = eventService.uploadCover(id, file);
        return ResponseEntity.ok(eventMapper.toDto(event));
    }

    @PostMapping("/{id}/upload-gallery")
    public ResponseEntity<EventDTO> uploadGallery(
            @PathVariable("id") String id,
            @RequestParam("file") MultipartFile[] file)
            throws ResourceNotFoundException, IOException {
        Event event = eventService.uploadGallery(id, file);
        return ResponseEntity.ok(eventMapper.toDto(event));
    }

    @DeleteMapping("/{id}/gallery/{photoName}")
    public ResponseEntity<EventDTO> deleteGallery(@PathVariable("id") String id,
            @PathVariable("photoName") String photoName)
            throws ResourceNotFoundException, IOException {
        Event event = eventService.deleteGallery(id, photoName);
        return ResponseEntity.ok(eventMapper.toDto(event));
    }

    // Iscrizione di un utente all'evento
    @PostMapping("/{eventId}/register/{userId}")
    @Operation(summary = "Iscrizione di un utente a un evento", description = "Permette a un utente di registrarsi a un evento specificato tramite eventId.", parameters = {
            @Parameter(name = "eventId", description = "ID dell'evento a cui l'utente si deve iscrivere", required = true),
            @Parameter(name = "userId", description = "ID dell'utente che si deve iscrivere all'evento", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Utente registrato con successo"),
            @ApiResponse(responseCode = "400", description = "Errore nella richiesta, ad esempio, l'utente è già registrato a questo evento"),
            @ApiResponse(responseCode = "404", description = "Evento o utente non trovato")
    })
    public ResponseEntity<Map<String, Object>> registerUserToEvent(@PathVariable("eventId") String eventId,
            @PathVariable("userId") String userId) throws ResourceNotFoundException,
            BadRequestException {
        eventService.registerUserToEvent(eventId, userId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.OK.value());
        body.put("message", "Utente registrato con successo.");
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    // Cancellazione dell'iscrizione di un utente
    @PostMapping("/{eventId}/unregister/{userId}")
    @Operation(summary = "Cancellazione dell'iscrizione di un utente", description = "Permette a un utente di cancellarsi da un evento specificato tramite eventId.", parameters = {
            @Parameter(name = "eventId", description = "ID dell'evento da cui l'utente si deve cancellare", required = true),
            @Parameter(name = "userId", description = "ID dell'utente che si cancella dall'evento", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Utente cancellato con successo"),
            @ApiResponse(responseCode = "400", description = "Errore nella richiesta, ad esempio, l'utente non è iscritto a questo evento"),
            @ApiResponse(responseCode = "404", description = "Evento o utente non trovato")
    })
    public ResponseEntity<Map<String, Object>> unregisterUserFromEvent(@PathVariable("eventId") String eventId,
            @PathVariable("userId") String userId) throws ResourceNotFoundException,
            BadRequestException {
        eventService.unregisterUserFromEvent(eventId, userId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.OK.value());
        body.put("message", "Utente cancellato con successo.");
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    // Check-in di un utente
    @PostMapping("/{eventId}/check-in/{userId}")
    @Operation(summary = "Check-in di un utente a un evento", description = "Permette a un utente di effettuare il check-in per un evento specificato tramite eventId.", parameters = {
            @Parameter(name = "eventId", description = "ID dell'evento a cui l'utente deve fare il check-in", required = true),
            @Parameter(name = "userId", description = "ID dell'utente che effettua il check-in", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Check-in completato con successo"),
            @ApiResponse(responseCode = "400", description = "Errore nella richiesta, ad esempio, l'utente non è registrato all'evento"),
            @ApiResponse(responseCode = "404", description = "Evento o utente non trovato")
    })
    public ResponseEntity<Map<String, Object>> checkInUser(@PathVariable("eventId") String eventId,
            @PathVariable("userId") String userId) throws ResourceNotFoundException,
            BadRequestException {
        eventService.checkInUser(eventId, userId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.OK.value());
        body.put("message", "Check-in completato con successo.");
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Recupera gli eventi a cui un utente è registrato", description = "Restituisce una lista paginata di eventi a cui un utente (identificato da userId) è registrato.", parameters = {
            @Parameter(name = "userId", description = "ID dell'utente per cui recuperare gli eventi", required = true)
    }, responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista di eventi dell'utente trovata e restituita"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Nessun evento trovato per l'utente")
    })
    public ResponseEntity<Page<EventDTO>> getUserEvents(@PathVariable("userId") String userId, Pageable pageable) {
        Page<Event> userEvents = eventService.getEventsByUserId(userId, pageable);
        return ResponseEntity.ok(userEvents.map(eventMapper::toDto));
    }
}
