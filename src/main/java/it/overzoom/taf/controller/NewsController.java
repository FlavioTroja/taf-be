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
import org.springframework.data.domain.Sort;
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
import it.overzoom.taf.dto.NewsDTO;
import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.mapper.NewsMapper;
import it.overzoom.taf.model.News;
import it.overzoom.taf.model.User;
import it.overzoom.taf.service.NewsService;
import it.overzoom.taf.service.UserService;
import it.overzoom.taf.utils.SecurityUtils;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/news")
public class NewsController extends BaseSearchController<News, NewsDTO> {

    private static final Logger log = LoggerFactory.getLogger(NewsController.class);
    private final NewsService newsService;
    private final NewsMapper newsMapper;
    private final UserService userService;

    public NewsController(NewsService newsService, NewsMapper newsMapper, UserService userService) {
        this.newsService = newsService;
        this.newsMapper = newsMapper;
        this.userService = userService;
    }

    @Override
    protected String getCollectionName() {
        return "news";
    }

    @Override
    protected Class<News> getEntityClass() {
        return News.class;
    }

    @Override
    protected Function<News, NewsDTO> toDtoMapper() {
        return newsMapper::toDto;
    }

    @Override
    protected List<String> getSearchableFields() {
        return List.of("title", "content", "author", "tags", "municipalityId");
    }

    @Override
    protected Sort buildSort(Map<String, String> sortMap) {
        if (sortMap == null || sortMap.isEmpty()) {
            return Sort.by(Sort.Order.desc("created")); // Ordinamento predefinito per le news
        }
        return super.buildSort(sortMap); // Utilizza la logica di ordinamento di BaseSearchController
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
    @Operation(summary = "Recupera una notizia per ID", description = "Restituisce i dettagli di una notizia specifica utilizzando l'ID", parameters = @Parameter(name = "id", description = "ID della notizia", required = true), responses = {
            @ApiResponse(responseCode = "200", description = "Notizia trovata e restituita"),
            @ApiResponse(responseCode = "404", description = "Notizia non trovata con questo ID")
    })
    public ResponseEntity<NewsDTO> findById(@PathVariable("id") String id) throws ResourceNotFoundException {
        return newsService.findById(id)
                .map(newsMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Notizia non trovata."));
    }

    @PostMapping("/create")
    @Operation(summary = "Crea una nuova notizia", description = "Crea una nuova notizia. L'ID non deve essere fornito per una nuova notizia", responses = {
            @ApiResponse(responseCode = "201", description = "Notizia creata con successo"),
            @ApiResponse(responseCode = "400", description = "ID fornito erroneamente per una nuova notizia")
    })
    public ResponseEntity<NewsDTO> create(@Valid @RequestBody NewsDTO newsDTO)
            throws BadRequestException, URISyntaxException {
        log.info("REST request to save News : {}", newsDTO);
        if (newsDTO.getId() != null) {
            throw new BadRequestException("Una nuova notizia non può già avere un ID");
        }
        News news = newsMapper.toEntity(newsDTO);
        news = newsService.create(news);
        return ResponseEntity.created(new URI("/api/news/" + news.getId())).body(newsMapper.toDto(news));
    }

    @PutMapping("")
    @Operation(summary = "Aggiorna una notizia", description = "Aggiorna una notizia esistente. L'ID deve essere fornito per identificare la notizia da aggiornare", responses = {
            @ApiResponse(responseCode = "200", description = "Notizia aggiornata con successo"),
            @ApiResponse(responseCode = "400", description = "ID non valido o mancante"),
            @ApiResponse(responseCode = "404", description = "Notizia non trovata con l'ID fornito")
    })
    public ResponseEntity<NewsDTO> update(@Valid @RequestBody NewsDTO newsDTO)
            throws BadRequestException, ResourceNotFoundException {
        log.info("REST request to update News: {}", newsDTO);
        if (newsDTO.getId() == null) {
            throw new BadRequestException("ID invalido.");
        }
        if (!newsService.existsById(newsDTO.getId())) {
            throw new ResourceNotFoundException("Notizia non trovata.");
        }
        News news = newsMapper.toEntity(newsDTO);
        News updated = newsService.update(news)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notizia non trovata con questo ID :: " + news.getId()));

        return ResponseEntity.ok().body(newsMapper.toDto(updated));
    }

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @Operation(summary = "Aggiorna parzialmente una notizia", description = "Aggiorna parzialmente una notizia esistente, con la possibilità di aggiornare solo i campi forniti", parameters = @Parameter(name = "id", description = "ID della notizia da aggiornare", required = true), responses = {
            @ApiResponse(responseCode = "200", description = "Notizia parzialmente aggiornata"),
            @ApiResponse(responseCode = "404", description = "Notizia non trovata con l'ID fornito")
    })
    public ResponseEntity<NewsDTO> partialUpdate(@PathVariable("id") String id,
            @RequestBody NewsDTO newsDTO)
            throws BadRequestException, ResourceNotFoundException {
        log.info("REST request to partial update News: {}", newsDTO);
        if (id == null) {
            throw new BadRequestException("ID invalido.");
        }
        if (!newsService.existsById(id)) {
            throw new ResourceNotFoundException("Notizia non trovata.");
        }
        News news = newsMapper.toEntity(newsDTO);
        News updated = newsService.partialUpdate(id, news)
                .orElseThrow(() -> new ResourceNotFoundException("Notizia non trovata con questo ID :: " + id));

        return ResponseEntity.ok().body(newsMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancella una notizia", description = "Cancella la notizia specificata tramite ID", parameters = @Parameter(name = "id", description = "ID della notizia da eliminare", required = true), responses = {
            @ApiResponse(responseCode = "204", description = "Notizia eliminata con successo"),
            @ApiResponse(responseCode = "404", description = "Notizia non trovata con l'ID fornito")
    })
    public ResponseEntity<Void> deleteById(@PathVariable("id") String id) throws ResourceNotFoundException {
        log.info("REST request to delete News with ID: {}", id);
        if (!newsService.existsById(id)) {
            throw new ResourceNotFoundException("Notizia non trovata.");
        }
        newsService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/upload-cover")
    @Operation(summary = "Carica un'immagine di copertura per una notizia", description = "Carica un'immagine di copertura per la notizia specificata tramite ID", parameters = {
            @Parameter(name = "id", description = "ID della notizia a cui associare l'immagine di copertura", required = true),
            @Parameter(name = "file", description = "File dell'immagine di copertura", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Immagine di copertura caricata con successo"),
            @ApiResponse(responseCode = "404", description = "Notizia non trovata con l'ID fornito")
    })
    public ResponseEntity<NewsDTO> uploadCover(@PathVariable("id") String id,
            @RequestParam("file") MultipartFile file)
            throws ResourceNotFoundException, IOException {
        News news = newsService.uploadCover(id, file);
        return ResponseEntity.ok(newsMapper.toDto(news));
    }

    @PostMapping("/{id}/upload-gallery")
    @Operation(summary = "Carica una galleria fotografica per una notizia", description = "Carica una o più foto nella galleria della notizia specificata tramite ID", parameters = {
            @Parameter(name = "id", description = "ID della notizia a cui aggiungere foto", required = true),
            @Parameter(name = "file", description = "File delle foto da caricare", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Foto caricate con successo nella galleria"),
            @ApiResponse(responseCode = "404", description = "Notizia non trovata con l'ID fornito")
    })
    public ResponseEntity<NewsDTO> uploadGallery(
            @PathVariable("id") String id,
            @RequestParam("file") MultipartFile[] file)
            throws ResourceNotFoundException, IOException {
        News news = newsService.uploadGallery(id, file);
        return ResponseEntity.ok(newsMapper.toDto(news));
    }

    @DeleteMapping("/{id}/gallery/{photoName}")
    @Operation(summary = "Elimina una foto dalla galleria di una notizia", description = "Rimuove una foto dalla galleria della notizia specificata tramite ID", parameters = {
            @Parameter(name = "id", description = "ID della notizia da cui eliminare la foto", required = true),
            @Parameter(name = "photoName", description = "Nome della foto da eliminare", required = true)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Foto eliminata con successo"),
            @ApiResponse(responseCode = "404", description = "Notizia o foto non trovata")
    })
    public ResponseEntity<NewsDTO> deleteGallery(@PathVariable("id") String id,
            @PathVariable("photoName") String photoName)
            throws ResourceNotFoundException, IOException {
        News news = newsService.deleteGallery(id, photoName);
        return ResponseEntity.ok(newsMapper.toDto(news));
    }

}
