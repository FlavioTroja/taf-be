package it.overzoom.taf.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.overzoom.taf.dto.NotificationDTO;
import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.mapper.NotificationMapper;
import it.overzoom.taf.model.Notification;
import it.overzoom.taf.model.User;
import it.overzoom.taf.service.FCMService;
import it.overzoom.taf.service.NotificationService;
import it.overzoom.taf.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController extends BaseSearchController<Notification, NotificationDTO> {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;
    private final FCMService fcmService;
    private final UserService userService;
    private final NotificationMapper notificationMapper;

    public NotificationController(NotificationService notificationService, UserService userService,
            NotificationMapper notificationMapper, FCMService fcmService) {
        this.notificationService = notificationService;
        this.userService = userService;
        this.notificationMapper = notificationMapper;
        this.fcmService = fcmService;
    }

    @Override
    protected String getCollectionName() {
        return "notification";
    }

    @Override
    protected Class<Notification> getEntityClass() {
        return Notification.class;
    }

    @Override
    protected Function<Notification, NotificationDTO> toDtoMapper() {
        return notificationMapper::toDto;
    }

    @Override
    protected List<String> getSearchableFields() {
        return List.of("message", "senderId", "receiverId", "municipalityId");
    }

    @GetMapping("")
    @Operation(summary = "Recupera una lista di notifiche", description = "Restituisce una lista paginata di tutte le notifiche", responses = {
            @ApiResponse(responseCode = "200", description = "Lista di notifiche trovata e restituita"),
            @ApiResponse(responseCode = "204", description = "Nessuna notifica trovata")
    })
    public ResponseEntity<Page<NotificationDTO>> findAll(Pageable pageable) {
        log.info("REST request to get a page of Notifications");
        Page<Notification> page = notificationService.findAll(pageable);
        return ResponseEntity.ok().body(page.map(notificationMapper::toDto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recupera una notifica per ID", description = "Restituisce i dettagli di una notifica specifica utilizzando l'ID", parameters = @Parameter(name = "id", description = "ID della notifica", required = true), responses = {
            @ApiResponse(responseCode = "200", description = "Notifica trovata e restituita"),
            @ApiResponse(responseCode = "404", description = "Notifica non trovata con questo ID")
    })
    public ResponseEntity<NotificationDTO> findById(@PathVariable("id") String id) throws ResourceNotFoundException {
        return notificationService.findById(id)
                .map(notificationMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Notifica non trovata."));
    }

    @PostMapping("/create")
    @Operation(summary = "Crea una nuova notifica", description = "Crea una nuova notifica. L'ID non deve essere fornito per una nuova notifica", responses = {
            @ApiResponse(responseCode = "201", description = "Notifica creata con successo"),
            @ApiResponse(responseCode = "400", description = "ID fornito erroneamente per una nuova notifica"),
            @ApiResponse(responseCode = "404", description = "Utente non trovato per l'invio della notifica")
    })
    public ResponseEntity<NotificationDTO> create(@Valid @RequestBody NotificationDTO notificationDTO)
            throws BadRequestException, URISyntaxException, ResourceNotFoundException {
        log.info("REST request to save Notification : {}", notificationDTO);
        if (notificationDTO.getId() != null) {
            throw new BadRequestException("Una nuova notifica non può già avere un ID");
        }
        User sender = userService.getProfile();
        if (notificationDTO.getSenderId() == null) {
            notificationDTO.setSenderId(sender.getId());
        } else if (!notificationDTO.getSenderId().equals(sender.getId())) {
            throw new BadRequestException("Non puoi inviare notifiche per altri utenti.");
        }
        Notification notification = notificationMapper.toEntity(notificationDTO);
        notification = notificationService.create(notification);
        return ResponseEntity.created(new URI("/api/notifications/" + notification.getId()))
                .body(notificationMapper.toDto(notification));
    }

    @PutMapping("")
    @Operation(summary = "Aggiorna una notifica", description = "Aggiorna una notifica esistente. L'ID deve essere fornito per identificare la notifica da aggiornare", responses = {
            @ApiResponse(responseCode = "200", description = "Notifica aggiornata con successo"),
            @ApiResponse(responseCode = "400", description = "ID non valido o mancante"),
            @ApiResponse(responseCode = "404", description = "Notifica non trovata con l'ID fornito")
    })
    public ResponseEntity<NotificationDTO> update(@Valid @RequestBody NotificationDTO notificationDTO)
            throws BadRequestException, ResourceNotFoundException {
        log.info("REST request to update Notification: {}", notificationDTO);
        if (notificationDTO.getId() == null) {
            throw new BadRequestException("ID invalido.");
        }
        if (!notificationService.existsById(notificationDTO.getId())) {
            throw new ResourceNotFoundException("Notifica non trovata.");
        }
        Notification notification = notificationMapper.toEntity(notificationDTO);
        Notification updated = notificationService.update(notification)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notifica non trovata con questo ID :: " + notification.getId()));

        return ResponseEntity.ok().body(notificationMapper.toDto(updated));
    }

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @Operation(summary = "Aggiorna parzialmente una notifica", description = "Aggiorna parzialmente una notifica esistente, con la possibilità di aggiornare solo i campi forniti", parameters = @Parameter(name = "id", description = "ID della notifica da aggiornare", required = true), responses = {
            @ApiResponse(responseCode = "200", description = "Notifica parzialmente aggiornata"),
            @ApiResponse(responseCode = "404", description = "Notifica non trovata con l'ID fornito")
    })
    public ResponseEntity<NotificationDTO> partialUpdate(@PathVariable("id") String id,
            @RequestBody NotificationDTO notificationDTO)
            throws BadRequestException, ResourceNotFoundException {
        log.info("REST request to partial update Notification: {}", notificationDTO);
        if (id == null) {
            throw new BadRequestException("ID invalido.");
        }
        if (!notificationService.existsById(id)) {
            throw new ResourceNotFoundException("Notifica non trovata.");
        }
        Notification notification = notificationMapper.toEntity(notificationDTO);
        Notification updated = notificationService.partialUpdate(id, notification)
                .orElseThrow(() -> new ResourceNotFoundException("Notifica non trovata con questo ID :: " + id));

        return ResponseEntity.ok().body(notificationMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancella una notifica", description = "Cancella la notifica specificata tramite ID", parameters = @Parameter(name = "id", description = "ID della notifica da eliminare", required = true), responses = {
            @ApiResponse(responseCode = "204", description = "Notifica eliminata con successo"),
            @ApiResponse(responseCode = "404", description = "Notifica non trovata con l'ID fornito")
    })
    public ResponseEntity<Void> deleteById(@PathVariable("id") String id) throws ResourceNotFoundException {
        log.info("REST request to delete Notification with ID: {}", id);
        if (!notificationService.existsById(id)) {
            throw new ResourceNotFoundException("Notifica non trovata.");
        }
        notificationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/latest")
    @Operation(summary = "Recupera le ultime notifiche", description = "Restituisce le ultime notifiche inviate, limitando il numero tramite il parametro 'size'.", parameters = @Parameter(name = "size", description = "Numero di notifiche da recuperare", required = false), responses = {
            @ApiResponse(responseCode = "200", description = "Ultime notifiche trovate e restituite"),
            @ApiResponse(responseCode = "204", description = "Nessuna notifica trovata")
    })
    public ResponseEntity<Page<NotificationDTO>> getLatestNotifications(@RequestParam(defaultValue = "5") int size) {
        log.info("REST request to get the latest {} notifications", size);
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<Notification> page = notificationService.findAll(pageable);
        return ResponseEntity.ok().body(page.map(notificationMapper::toDto));
    }

    @PostMapping("/send")
    public String sendNotification(
            @RequestParam String token,
            @RequestParam String title,
            @RequestParam String body,
            @RequestBody(required = false) Map<String, String> data) throws IOException {
        boolean sent = fcmService.sendNotification(token, title, body, data != null ? data : Map.of());
        return sent ? "Notification sent!" : "Failed to send notification.";
    }
}
