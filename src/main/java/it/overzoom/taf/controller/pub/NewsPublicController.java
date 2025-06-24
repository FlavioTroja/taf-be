package it.overzoom.taf.controller.pub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.overzoom.taf.dto.NewsDTO;
import it.overzoom.taf.mapper.NewsMapper;
import it.overzoom.taf.model.News;
import it.overzoom.taf.service.NewsService;

@RestController
@RequestMapping("/api/public/news")
public class NewsPublicController {

    private static final Logger log = LoggerFactory.getLogger(NewsPublicController.class);
    private final NewsService newsService;
    private final NewsMapper newsMapper;

    public NewsPublicController(NewsService newsService, NewsMapper newsMapper) {
        this.newsService = newsService;
        this.newsMapper = newsMapper;
    }

    @GetMapping("/latest")
    @Operation(summary = "Recupera le ultime notizie", description = "Restituisce una lista paginata delle ultime notizie in ordine decrescente per data di pubblicazione", parameters = {
            @Parameter(name = "size", description = "Numero di notizie da restituire, predefinito a 5", required = false)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Lista delle ultime notizie trovate e restituita"),
            @ApiResponse(responseCode = "204", description = "Nessuna notizia trovata")
    })
    public ResponseEntity<Page<NewsDTO>> getLatestNews(@RequestParam(defaultValue = "5") int size) {
        log.info("REST request to get the latest {} news", size);
        Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "publicationDate"));
        Page<News> page = newsService.findAll(pageable);
        return ResponseEntity.ok().body(page.map(newsMapper::toDto));
    }
}
