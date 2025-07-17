package it.overzoom.taf.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.model.News;

public interface NewsService {

    Page<News> findAll(Pageable pageable);

    Optional<News> findById(String id);

    boolean existsById(String id);

    News create(News news);

    Optional<News> update(News news);

    Optional<News> partialUpdate(String id, News news);

    void deleteById(String id);

    News uploadCover(String newsId, MultipartFile file) throws IOException, ResourceNotFoundException;

    News uploadGallery(String newsId, MultipartFile[] files) throws IOException, ResourceNotFoundException;

    News deleteGallery(String newsId, String photoName) throws IOException, ResourceNotFoundException;
}
