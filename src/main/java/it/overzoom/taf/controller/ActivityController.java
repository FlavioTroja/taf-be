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
import org.springframework.data.mongodb.core.query.Criteria;
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
import it.overzoom.taf.dto.ActivityDTO;
import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.mapper.ActivityMapper;
import it.overzoom.taf.model.Activity;
import it.overzoom.taf.model.User;
import it.overzoom.taf.service.ActivityService;
import it.overzoom.taf.service.UserService;
import it.overzoom.taf.utils.SecurityUtils;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/activities")
public class ActivityController extends BaseSearchController<Activity, ActivityDTO> {

    private static final Logger log = LoggerFactory.getLogger(ActivityController.class);
    private final ActivityService activityService;
    private final ActivityMapper activityMapper;
    private final UserService userService;

    public ActivityController(
            ActivityService activityService,
            ActivityMapper activityMapper,
            UserService userService) {
        this.activityService = activityService;
        this.activityMapper = activityMapper;
        this.userService = userService;
    }

    @Override
    protected String getCollectionName() {
        return "activity";
    }

    @Override
    protected Class<Activity> getEntityClass() {
        return Activity.class;
    }

    @Override
    protected Function<Activity, ActivityDTO> toDtoMapper() {
        return activityMapper::toDto;
    }

    @Override
    protected List<String> getSearchableFields() {
        return List.of("name", "address", "description", "municipalityId", "type", "tags");
    }

    @Override
    protected List<Criteria> getExtraCriteriaForCurrentUser(Map<String, Object> request) {
        try {
            if (SecurityUtils.isAdmin())
                return List.of();

            String userId = SecurityUtils.getCurrentUserId();
            User user = userService.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));
            String[] allowedMunicipalityIds = user.getMunicipalityIds();
            if (allowedMunicipalityIds == null || allowedMunicipalityIds.length == 0) {
                return List.of(Criteria.where("municipalityId").is("__NO_MATCH__"));
            }

            Map<String, Object> filtersObj = (Map<String, Object>) request.get("filters");
            List<String> filteredIds = null;
            if (filtersObj != null && filtersObj.containsKey("municipalityIds")) {
                Object val = filtersObj.get("municipalityIds");
                if (val instanceof List<?> list) {
                    filteredIds = list.stream()
                            .map(Object::toString)
                            .filter(id -> java.util.Arrays.asList(allowedMunicipalityIds).contains(id))
                            .toList();
                }
            }

            List<String> actualIds = (filteredIds != null) ? filteredIds : List.of(allowedMunicipalityIds);

            if (actualIds.isEmpty()) {
                return List.of(Criteria.where("municipalityId").is("__NO_MATCH__"));
            }
            return List.of(Criteria.where("municipalityId").in(actualIds));
        } catch (ResourceNotFoundException ex) {
            // Utente non autenticato: non restituire nulla per sicurezza
            return List.of(Criteria.where("municipalityId").is("__NO_MATCH__"));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recupera un'attività per ID", description = "Restituisce i dettagli di un'attività specifica utilizzando l'ID", parameters = @Parameter(name = "id", description = "ID dell'attività", required = true), responses = {
            @ApiResponse(responseCode = "200", description = "Attività trovata e restituita"),
            @ApiResponse(responseCode = "404", description = "Attività non trovata con questo ID")
    })
    public ResponseEntity<ActivityDTO> findById(@PathVariable("id") String id) throws ResourceNotFoundException {
        return activityService.findById(id)
                .map(activityMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Attività non trovata."));
    }

    @PostMapping("/create")
    @Operation(summary = "Crea una nuova attività", description = "Crea un'attività nuova. Il campo ID non deve essere fornito", responses = {
            @ApiResponse(responseCode = "201", description = "Attività creata con successo"),
            @ApiResponse(responseCode = "400", description = "ID fornito erroneamente per una nuova attività")
    })
    public ResponseEntity<ActivityDTO> create(@Valid @RequestBody ActivityDTO activityDTO)
            throws BadRequestException, URISyntaxException {
        log.info("REST request to save Activity : {}", activityDTO);
        if (activityDTO.getId() != null) {
            throw new BadRequestException("Una nuova attività non può già avere un ID");
        }
        Activity activity = activityMapper.toEntity(activityDTO);
        activity = activityService.create(activity);
        return ResponseEntity.created(new URI("/api/activities/" + activity.getId()))
                .body(activityMapper.toDto(activity));
    }

    @PutMapping("")
    @Operation(summary = "Aggiorna un'attività", description = "Aggiorna un'attività esistente. L'ID deve essere fornito per identificare l'attività da aggiornare", responses = {
            @ApiResponse(responseCode = "200", description = "Attività aggiornata con successo"),
            @ApiResponse(responseCode = "400", description = "ID invalido o mancante"),
            @ApiResponse(responseCode = "404", description = "Attività non trovata con l'ID fornito")
    })
    public ResponseEntity<ActivityDTO> update(@Valid @RequestBody ActivityDTO activityDTO)
            throws BadRequestException, ResourceNotFoundException {
        log.info("REST request to update Activity: {}", activityDTO);
        if (activityDTO.getId() == null) {
            throw new BadRequestException("ID invalido.");
        }
        if (!activityService.existsById(activityDTO.getId())) {
            throw new ResourceNotFoundException("Attività non trovata.");
        }
        Activity activity = activityMapper.toEntity(activityDTO);
        Activity updated = activityService.update(activity)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attività non trovata con questo ID :: " + activity.getId()));

        return ResponseEntity.ok().body(activityMapper.toDto(updated));
    }

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @Operation(summary = "Aggiorna parzialmente un'attività", description = "Aggiorna parzialmente i dettagli di un'attività esistente. Solo i campi forniti vengono aggiornati.", parameters = @Parameter(name = "id", description = "ID dell'attività da aggiornare", required = true), responses = {
            @ApiResponse(responseCode = "200", description = "Attività aggiornata con successo"),
            @ApiResponse(responseCode = "404", description = "Attività non trovata con l'ID fornito")
    })
    public ResponseEntity<ActivityDTO> partialUpdate(@PathVariable("id") String id,
            @RequestBody ActivityDTO activityDTO)
            throws BadRequestException, ResourceNotFoundException {
        log.info("REST request to partial update Activity: {}", activityDTO);
        if (id == null) {
            throw new BadRequestException("ID invalido.");
        }
        if (!activityService.existsById(id)) {
            throw new ResourceNotFoundException("Attività non trovata.");
        }
        Activity activity = activityMapper.toEntity(activityDTO);
        Activity updated = activityService.partialUpdate(id, activity)
                .orElseThrow(() -> new ResourceNotFoundException("Attività non trovata con questo ID :: " + id));

        return ResponseEntity.ok().body(activityMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancella un'attività per ID", description = "Elimina un'attività specificata tramite ID", parameters = @Parameter(name = "id", description = "ID dell'attività da eliminare", required = true), responses = {
            @ApiResponse(responseCode = "204", description = "Attività eliminata con successo"),
            @ApiResponse(responseCode = "404", description = "Attività non trovata con l'ID fornito")
    })
    public ResponseEntity<Void> deleteById(@PathVariable("id") String id) throws ResourceNotFoundException {
        log.info("REST request to delete Activity with ID: {}", id);
        if (!activityService.existsById(id)) {
            throw new ResourceNotFoundException("Attività non trovata.");
        }
        activityService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/upload-logo")
    @Operation(summary = "Carica il logo dell'attività", description = "Carica un logo per l'attività specificata tramite ID", parameters = {
            @Parameter(name = "id", description = "ID dell'attività a cui associare il logo", required = true),
            @Parameter(name = "file", description = "File del logo da caricare", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Logo caricato con successo"),
            @ApiResponse(responseCode = "404", description = "Attività non trovata con l'ID fornito")
    })
    public ResponseEntity<ActivityDTO> uploadLogo(@PathVariable("id") String id,
            @RequestParam("file") MultipartFile file)
            throws ResourceNotFoundException, IOException {
        Activity activity = activityService.uploadLogo(id, file);
        return ResponseEntity.ok(activityMapper.toDto(activity));
    }

    @PostMapping("/{id}/upload-cover")
    @Operation(summary = "Carica l'immagine di copertura dell'attività", description = "Carica un'immagine di copertura per l'attività specificata tramite ID", parameters = {
            @Parameter(name = "id", description = "ID dell'attività a cui associare l'immagine di copertura", required = true),
            @Parameter(name = "file", description = "File dell'immagine di copertura da caricare", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Immagine di copertura caricata con successo"),
            @ApiResponse(responseCode = "404", description = "Attività non trovata con l'ID fornito")
    })
    public ResponseEntity<ActivityDTO> uploadCover(@PathVariable("id") String id,
            @RequestParam("file") MultipartFile file)
            throws ResourceNotFoundException, IOException {
        Activity activity = activityService.uploadCover(id, file);
        return ResponseEntity.ok(activityMapper.toDto(activity));
    }

    @PostMapping("/{id}/upload-gallery")
    @Operation(summary = "Carica galleria fotografica dell'attività", description = "Carica foto per la galleria dell'attività specificata tramite ID", parameters = {
            @Parameter(name = "id", description = "ID dell'attività per cui caricare le foto della galleria", required = true),
            @Parameter(name = "file", description = "File delle foto da caricare", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Foto della galleria caricate con successo"),
            @ApiResponse(responseCode = "404", description = "Attività non trovata con l'ID fornito")
    })
    public ResponseEntity<ActivityDTO> uploadGallery(
            @PathVariable("id") String id,
            @RequestParam("file") MultipartFile[] file)
            throws ResourceNotFoundException, IOException {
        Activity activity = activityService.uploadGallery(id, file);
        return ResponseEntity.ok(activityMapper.toDto(activity));
    }

    @DeleteMapping("/{id}/gallery/{photoName}")
    @Operation(summary = "Cancella una foto dalla galleria", description = "Elimina una foto specifica dalla galleria dell'attività", parameters = {
            @Parameter(name = "id", description = "ID dell'attività da cui eliminare la foto", required = true),
            @Parameter(name = "photoName", description = "Nome della foto da eliminare", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Foto eliminata con successo"),
            @ApiResponse(responseCode = "404", description = "Attività non trovata o foto non trovata")
    })
    public ResponseEntity<ActivityDTO> deleteGallery(@PathVariable("id") String id,
            @PathVariable("photoName") String photoName)
            throws ResourceNotFoundException, IOException {
        Activity activity = activityService.deleteGallery(id, photoName);
        return ResponseEntity.ok(activityMapper.toDto(activity));
    }

}
