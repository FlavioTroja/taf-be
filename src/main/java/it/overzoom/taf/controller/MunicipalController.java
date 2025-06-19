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

import it.overzoom.taf.dto.MunicipalDTO;
import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.mapper.MunicipalMapper;
import it.overzoom.taf.model.Municipal;
import it.overzoom.taf.service.MunicipalService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/municipals")
public class MunicipalController extends BaseSearchController<Municipal, MunicipalDTO> {

    private static final Logger log = LoggerFactory.getLogger(MunicipalController.class);
    private final MunicipalService municipalService;
    private final MunicipalMapper municipalMapper;

    public MunicipalController(MunicipalService municipalService, MunicipalMapper municipalMapper) {
        this.municipalService = municipalService;
        this.municipalMapper = municipalMapper;
    }

    @Override
    protected String getCollectionName() {
        return "municipal";
    }

    @Override
    protected Class<Municipal> getEntityClass() {
        return Municipal.class;
    }

    @Override
    protected Function<Municipal, MunicipalDTO> toDtoMapper() {
        return municipalMapper::toDto;
    }

    @GetMapping("")
    public ResponseEntity<Page<MunicipalDTO>> findAll(Pageable pageable) {
        log.info("REST request to get a page of Municipals");
        Page<Municipal> page = municipalService.findAll(pageable);
        return ResponseEntity.ok().body(page.map(municipalMapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MunicipalDTO> findById(@PathVariable("id") String id) throws ResourceNotFoundException {
        return municipalService.findById(id)
                .map(municipalMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Comune non trovato."));
    }

    @PostMapping("/create")
    public ResponseEntity<MunicipalDTO> create(@Valid @RequestBody MunicipalDTO municipalDTO)
            throws BadRequestException, URISyntaxException {
        log.info("REST request to save Municipal : {}", municipalDTO);
        if (municipalDTO.getId() != null) {
            throw new BadRequestException("Un nuovo comune non può già avere un ID");
        }
        Municipal municipal = municipalMapper.toEntity(municipalDTO);
        municipal = municipalService.create(municipal);
        return ResponseEntity.created(new URI("/api/municipals/" + municipal.getId()))
                .body(municipalMapper.toDto(municipal));
    }

    @PutMapping("")
    public ResponseEntity<MunicipalDTO> update(@Valid @RequestBody MunicipalDTO municipalDTO)
            throws BadRequestException, ResourceNotFoundException {
        log.info("REST request to update Municipal: {}", municipalDTO);
        if (municipalDTO.getId() == null) {
            throw new BadRequestException("ID invalido.");
        }
        if (!municipalService.existsById(municipalDTO.getId())) {
            throw new ResourceNotFoundException("Comune non trovato.");
        }
        Municipal municipal = municipalMapper.toEntity(municipalDTO);
        Municipal updated = municipalService.update(municipal)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Comune non trovato con questo ID :: " + municipal.getId()));

        return ResponseEntity.ok().body(municipalMapper.toDto(updated));
    }

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MunicipalDTO> partialUpdate(@PathVariable("id") String id,
            @RequestBody MunicipalDTO municipalDTO)
            throws BadRequestException, ResourceNotFoundException {
        log.info("REST request to partial update Municipal: {}", municipalDTO);
        if (id == null) {
            throw new BadRequestException("ID invalido.");
        }
        if (!municipalService.existsById(id)) {
            throw new ResourceNotFoundException("Comune non trovato.");
        }
        Municipal municipal = municipalMapper.toEntity(municipalDTO);
        Municipal updated = municipalService.partialUpdate(id, municipal)
                .orElseThrow(() -> new ResourceNotFoundException("Comune non trovato con questo ID :: " + id));

        return ResponseEntity.ok().body(municipalMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") String id) throws ResourceNotFoundException {
        log.info("REST request to delete Municipal with ID: {}", id);
        if (!municipalService.existsById(id)) {
            throw new ResourceNotFoundException("Comune non trovato.");
        }
        municipalService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
