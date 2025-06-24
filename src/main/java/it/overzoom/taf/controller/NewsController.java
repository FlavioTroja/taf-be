package it.overzoom.taf.controller;

import java.io.IOException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.overzoom.taf.dto.NewsDTO;
import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.mapper.NewsMapper;
import it.overzoom.taf.model.News;
import it.overzoom.taf.service.NewsService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/news")
public class NewsController extends BaseSearchController<News, NewsDTO> {

    private static final Logger log = LoggerFactory.getLogger(NewsController.class);
    private final NewsService newsService;
    private final NewsMapper newsMapper;

    public NewsController(NewsService newsService, NewsMapper newsMapper) {
        this.newsService = newsService;
        this.newsMapper = newsMapper;
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

    @GetMapping("")
    public ResponseEntity<Page<NewsDTO>> findAll(Pageable pageable) {
        log.info("REST request to get a page of News");
        Page<News> page = newsService.findAll(pageable);
        return ResponseEntity.ok().body(page.map(newsMapper::toDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsDTO> findById(@PathVariable("id") String id) throws ResourceNotFoundException {
        return newsService.findById(id)
                .map(newsMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Notizia non trovata."));
    }

    @PostMapping("/create")
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
    public ResponseEntity<Void> deleteById(@PathVariable("id") String id) throws ResourceNotFoundException {
        log.info("REST request to delete News with ID: {}", id);
        if (!newsService.existsById(id)) {
            throw new ResourceNotFoundException("Notizia non trovata.");
        }
        newsService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/upload-cover")
    public ResponseEntity<NewsDTO> uploadCover(@PathVariable("id") String id,
            @RequestParam("file") MultipartFile file)
            throws ResourceNotFoundException, IOException {
        News news = newsService.uploadCover(id, file);
        return ResponseEntity.ok(newsMapper.toDto(news));
    }

    @PostMapping("/{id}/upload-gallery")
    public ResponseEntity<NewsDTO> uploadGallery(
            @PathVariable("id") String id,
            @RequestParam("file") MultipartFile[] file)
            throws ResourceNotFoundException, IOException {
        News news = newsService.uploadGallery(id, file);
        return ResponseEntity.ok(newsMapper.toDto(news));
    }

    @DeleteMapping("/{id}/gallery/{photoName}")
    public ResponseEntity<NewsDTO> deleteGallery(@PathVariable("id") String id,
            @PathVariable("photoName") String photoName)
            throws ResourceNotFoundException, IOException {
        News news = newsService.deleteGallery(id, photoName);
        return ResponseEntity.ok(newsMapper.toDto(news));
    }

}
