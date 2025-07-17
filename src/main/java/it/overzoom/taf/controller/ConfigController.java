package it.overzoom.taf.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.overzoom.taf.dto.ConfigDTO;
import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.mapper.ConfigMapper;
import it.overzoom.taf.model.Config;
import it.overzoom.taf.model.User;
import it.overzoom.taf.service.ConfigService;
import it.overzoom.taf.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/config/hidden")
public class ConfigController extends BaseSearchController<Config, ConfigDTO> {

    private static final Logger log = LoggerFactory.getLogger(ConfigController.class);
    private final ConfigService configService;
    private final UserService userService;
    private final ConfigMapper configMapper;

    public ConfigController(ConfigService configService, UserService userService, ConfigMapper configMapper) {
        this.configService = configService;
        this.userService = userService;
        this.configMapper = configMapper;
    }

    @Override
    protected String getCollectionName() {
        return "config";
    }

    @Override
    protected Class<Config> getEntityClass() {
        return Config.class;
    }

    @Override
    protected Function<Config, ConfigDTO> toDtoMapper() {
        return configMapper::toDto;
    }

    @Override
    protected List<String> getSearchableFields() {
        return List.of("context", "section", "component", "municipalityId");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recupera una configurazione per ID", description = "Restituisce una configurazione specifica utilizzando l'ID", parameters = @Parameter(name = "id", description = "ID della configurazione", required = true), responses = {
            @ApiResponse(responseCode = "200", description = "Configurazione trovata e restituita"),
            @ApiResponse(responseCode = "404", description = "Configurazione non trovata con questo ID")
    })
    public ResponseEntity<ConfigDTO> findById(@PathVariable("id") String id) throws ResourceNotFoundException {
        return configService.findById(id)
                .map(configMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Configurazione non trovata."));
    }

    @GetMapping("/own")
    @Operation(summary = "Recupera le configurazioni nascoste per l'utente", description = "Restituisce le configurazioni nascoste per l'utente corrente in base ai suoi ruoli", responses = {
            @ApiResponse(responseCode = "200", description = "Configurazioni nascoste per l'utente trovate e restituite"),
            @ApiResponse(responseCode = "404", description = "Nessuna configurazione trovata per l'utente")
    })
    public ResponseEntity<Map<String, Map<String, List<String>>>> getHiddenComponentsForUser()
            throws ResourceNotFoundException {
        log.info("REST request to get structured hidden configs for current user");

        User currentUser = userService.getProfile();
        List<Config> configs = configService.findByRoles(currentUser.getRoles());

        Map<String, Map<String, List<String>>> result = new HashMap<>();

        for (Config config : configs) {
            if (Boolean.TRUE.equals(config.getIsActive())
                    && config.getContext() != null
                    && config.getSection() != null
                    && config.getComponent() != null) {

                result
                        .computeIfAbsent(config.getContext(), ctx -> new HashMap<>())
                        .computeIfAbsent(config.getSection(), sec -> new ArrayList<>())
                        .add(config.getComponent());
            }
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/create")
    @Operation(summary = "Crea una nuova configurazione", description = "Crea una nuova configurazione. L'ID non deve essere fornito per una nuova configurazione", responses = {
            @ApiResponse(responseCode = "201", description = "Configurazione creata con successo"),
            @ApiResponse(responseCode = "400", description = "ID fornito erroneamente per una nuova configurazione")
    })
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
    @Operation(summary = "Aggiorna una configurazione", description = "Aggiorna una configurazione esistente. L'ID deve essere fornito per identificare la configurazione da aggiornare", responses = {
            @ApiResponse(responseCode = "200", description = "Configurazione aggiornata con successo"),
            @ApiResponse(responseCode = "400", description = "ID non valido o mancante"),
            @ApiResponse(responseCode = "404", description = "Configurazione non trovata con l'ID fornito")
    })
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
    @Operation(summary = "Aggiorna parzialmente una configurazione", description = "Aggiorna parzialmente una configurazione esistente, con la possibilità di aggiornare solo i campi forniti", parameters = @Parameter(name = "id", description = "ID della configurazione da aggiornare", required = true), responses = {
            @ApiResponse(responseCode = "200", description = "Configurazione parzialmente aggiornata"),
            @ApiResponse(responseCode = "404", description = "Configurazione non trovata con l'ID fornito")
    })
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
    @Operation(summary = "Cancella una configurazione", description = "Cancella la configurazione specificata tramite ID", parameters = @Parameter(name = "id", description = "ID della configurazione da eliminare", required = true), responses = {
            @ApiResponse(responseCode = "204", description = "Configurazione eliminata con successo"),
            @ApiResponse(responseCode = "404", description = "Configurazione non trovata con l'ID fornito")
    })
    public ResponseEntity<Void> deleteById(@PathVariable("id") String id) throws ResourceNotFoundException {
        log.info("REST request to delete Config with ID: {}", id);
        if (!configService.existsById(id)) {
            throw new ResourceNotFoundException("Configurazione non trovata.");
        }
        configService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
