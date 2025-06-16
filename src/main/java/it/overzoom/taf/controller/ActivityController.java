package it.overzoom.taf.controller;

import java.net.URI;
import java.net.URISyntaxException;
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

import it.overzoom.taf.dto.ActivityDTO;
import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.mapper.ActivityMapper;
import it.overzoom.taf.model.Activity;
import it.overzoom.taf.service.ActivityService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/activities")
public class ActivityController extends BaseSearchController<Activity, ActivityDTO> {

    private static final Logger log = LoggerFactory.getLogger(ActivityController.class);
    private final ActivityService activityService;
    private final ActivityMapper activityMapper;

    public ActivityController(
            ActivityService activityService,
            ActivityMapper activityMapper) {
        this.activityService = activityService;
        this.activityMapper = activityMapper;
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

    @GetMapping("")
    public ResponseEntity<Page<ActivityDTO>> findAll(Pageable pageable) {
        log.info("REST request to get a page of Activities");
        Page<Activity> page = activityService.findAll(pageable);
        return ResponseEntity.ok().body(page.map(activityMapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityDTO> findById(@PathVariable("id") String id) throws ResourceNotFoundException {
        return activityService.findById(id)
                .map(activityMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Attività non trovata."));
    }

    @PostMapping("/create")
    public ResponseEntity<Activity> create(@Valid @RequestBody ActivityDTO activityDTO)
            throws BadRequestException, URISyntaxException {
        log.info("REST request to save Activity : {}", activityDTO);
        if (activityDTO.getId() != null) {
            throw new BadRequestException("Una nuova attività non può già avere un ID");
        }
        Activity activity = activityMapper.toEntity(activityDTO);
        activity = activityService.create(activity);
        return ResponseEntity.created(new URI("/api/activities/" + activity.getId())).body(activity);
    }

    @PutMapping("")
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
    public ResponseEntity<Void> deleteById(@PathVariable("id") String id) throws ResourceNotFoundException {
        log.info("REST request to delete Activity with ID: {}", id);
        if (!activityService.existsById(id)) {
            throw new ResourceNotFoundException("Attività non trovata.");
        }
        activityService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
