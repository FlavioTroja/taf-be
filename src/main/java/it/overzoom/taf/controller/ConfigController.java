package it.overzoom.taf.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

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

import it.overzoom.taf.dto.ConfigDTO;
import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.mapper.Config;
import it.overzoom.taf.mapper.ConfigMapper;
import it.overzoom.taf.model.User;
import it.overzoom.taf.service.ConfigService;
import it.overzoom.taf.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/config/hidden")
public class ConfigController {

    private static final Logger log = LoggerFactory.getLogger(ConfigController.class);
    private final ConfigService configService;
    private final UserService userService;
    private final ConfigMapper configMapper;

    public ConfigController(ConfigService configService, UserService userService, ConfigMapper configMapper) {
        this.configService = configService;
        this.userService = userService;
        this.configMapper = configMapper;
    }

    @PostMapping("")
    public ResponseEntity<Page<ConfigDTO>> findAll(Pageable pageable) {
        log.info("REST request to get a page of Configs");
        Page<Config> page = configService.findAll(pageable);
        return ResponseEntity.ok().body(page.map(configMapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConfigDTO> findById(@PathVariable("id") String id) throws ResourceNotFoundException {
        return configService.findById(id)
                .map(configMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Configurazione non trovata."));
    }

    @GetMapping("/own")
    public ResponseEntity<List<ConfigDTO>> findOwn() throws ResourceNotFoundException {
        log.info("REST request to get own Configs");
        User currentUser = userService.getProfile();

        List<Config> configs = configService.findByRoles(currentUser.getRoles());

        if (configs.isEmpty()) {
            throw new ResourceNotFoundException("Nessuna configurazione trovata per i ruoli dell’utente.");
        }

        return ResponseEntity.ok(configs.stream().map(configMapper::toDto).toList());
    }

    @PostMapping("/create")
    public ResponseEntity<ConfigDTO> create(@Valid @RequestBody ConfigDTO configDTO)
            throws BadRequestException, URISyntaxException {
        log.info("REST request to save Config: {}", configDTO);
        if (configDTO.getId() != null) {
            throw new BadRequestException("Una nuova configurazione non può già avere un ID");
        }
        Config config = configMapper.toEntity(configDTO);
        config = configService.create(config);
        return ResponseEntity.created(new URI("/api/configs/" + config.getId())).body(configMapper.toDto(config));
    }

    @PutMapping("")
    public ResponseEntity<ConfigDTO> update(@Valid @RequestBody ConfigDTO configDTO)
            throws BadRequestException, ResourceNotFoundException {
        log.info("REST request to update Config: {}", configDTO);
        if (configDTO.getId() == null) {
            throw new BadRequestException("ID invalido.");
        }
        if (!configService.existsById(configDTO.getId())) {
            throw new ResourceNotFoundException("Configurazione non trovata.");
        }
        Config config = configMapper.toEntity(configDTO);
        Config updated = configService.update(config)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Configurazione non trovata con questo ID :: " + config.getId()));
        return ResponseEntity.ok().body(configMapper.toDto(updated));
    }

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ConfigDTO> partialUpdate(@PathVariable("id") String id,
            @RequestBody ConfigDTO configDTO)
            throws BadRequestException, ResourceNotFoundException {
        log.info("REST request to partial update Config: {}", configDTO);
        if (id == null) {
            throw new BadRequestException("ID invalido.");
        }
        if (!configService.existsById(id)) {
            throw new ResourceNotFoundException("Configurazione non trovata.");
        }
        Config config = configMapper.toEntity(configDTO);
        Config updated = configService.partialUpdate(id, config)
                .orElseThrow(() -> new ResourceNotFoundException("Configurazione non trovata con questo ID :: " + id));

        return ResponseEntity.ok().body(configMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") String id) throws ResourceNotFoundException {
        log.info("REST request to delete Config with ID: {}", id);
        if (!configService.existsById(id)) {
            throw new ResourceNotFoundException("Configurazione non trovata.");
        }
        configService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
