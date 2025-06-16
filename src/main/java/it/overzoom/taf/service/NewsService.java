package it.overzoom.taf.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import it.overzoom.taf.model.News;

public interface NewsService {

    Page<News> findAll(Pageable pageable);

    Optional<News> findById(String id);

    boolean existsById(String id);

    News create(News news);

    Optional<News> update(News news);

    Optional<News> partialUpdate(String id, News news);

    void deleteById(String id);
}
