package it.overzoom.taf.controller;

import java.net.URI;
import java.net.URISyntaxException;
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

import it.overzoom.taf.dto.NotificationDTO;
import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.mapper.NotificationMapper;
import it.overzoom.taf.model.Notification;
import it.overzoom.taf.model.User;
import it.overzoom.taf.service.NotificationService;
import it.overzoom.taf.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController extends BaseSearchController<Notification, NotificationDTO> {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;
    private final UserService userService;
    private final NotificationMapper notificationMapper;

    public NotificationController(NotificationService notificationService, UserService userService,
            NotificationMapper notificationMapper) {
        this.notificationService = notificationService;
        this.userService = userService;
        this.notificationMapper = notificationMapper;
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
    protected java.util.List<String> getSearchableFields() {
        return java.util.List.of("message", "senderId", "receiverId", "municipalityId");
    }

    @GetMapping("")
    public ResponseEntity<Page<NotificationDTO>> findAll(Pageable pageable) {
        log.info("REST request to get a page of Notifications");
        Page<Notification> page = notificationService.findAll(pageable);
        return ResponseEntity.ok().body(page.map(notificationMapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> findById(@PathVariable("id") String id) throws ResourceNotFoundException {
        return notificationService.findById(id)
                .map(notificationMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Notifica non trovata."));
    }

    @PostMapping("/create")
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
    public ResponseEntity<Void> deleteById(@PathVariable("id") String id) throws ResourceNotFoundException {
        log.info("REST request to delete Notification with ID: {}", id);
        if (!notificationService.existsById(id)) {
            throw new ResourceNotFoundException("Notifica non trovata.");
        }
        notificationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/latest")
    public ResponseEntity<Page<NotificationDTO>> getLatestNotifications(@RequestParam(defaultValue = "5") int size) {
        log.info("REST request to get the latest {} notifications", size);
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<Notification> page = notificationService.findAll(pageable);
        return ResponseEntity.ok().body(page.map(notificationMapper::toDto));
    }
}
