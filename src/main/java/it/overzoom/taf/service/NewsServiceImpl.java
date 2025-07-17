package it.overzoom.taf.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.model.News;
import it.overzoom.taf.repository.NewsRepository;
import it.overzoom.taf.type.EntityType;
import it.overzoom.taf.type.PhotoType;

@Service
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;
    private final PhotoService photoService;

    public NewsServiceImpl(NewsRepository newsRepository, PhotoService photoService) {
        this.newsRepository = newsRepository;
        this.photoService = photoService;
    }

    @Override
    public Page<News> findAll(Pageable pageable) {
        return newsRepository.findAll(pageable);
    }

    @Override
    public Optional<News> findById(String id) {
        return newsRepository.findById(id);
    }

    @Override
    public boolean existsById(String id) {
        return newsRepository.existsById(id);
    }

    @Override
    @Transactional
    public News create(News news) {
        return newsRepository.save(news);
    }

    @Override
    @Transactional
    public Optional<News> update(News news) {
        return newsRepository.findById(news.getId()).map(existing -> {
            existing.setTitle(news.getTitle());
            existing.setContent(news.getContent());
            existing.setAuthor(news.getAuthor());
            existing.setPublicationDate(news.getPublicationDate());
            existing.setTags(news.getTags());
            return existing;
        }).map(newsRepository::save);
    }

    @Override
    @Transactional
    public Optional<News> partialUpdate(String id, News news) {
        return newsRepository.findById(id).map(existing -> {
            if (news.getTitle() != null)
                existing.setTitle(news.getTitle());
            if (news.getContent() != null)
                existing.setContent(news.getContent());
            if (news.getAuthor() != null)
                existing.setAuthor(news.getAuthor());
            if (news.getPublicationDate() != null)
                existing.setPublicationDate(news.getPublicationDate());
            if (news.getTags() != null)
                existing.setTags(news.getTags());
            return existing;
        }).map(newsRepository::save);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        newsRepository.deleteById(id);
    }

    @Transactional
    public News uploadCover(String newsId, MultipartFile file) throws IOException, ResourceNotFoundException {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResourceNotFoundException("Notizia non trovata con ID: " + newsId));

        String path = photoService.uploadPhoto(EntityType.NEWS, newsId, file, PhotoType.COVER);
        news.setCover(path);
        newsRepository.save(news);
        return news;
    }

    @Transactional
    public News uploadGallery(String newsId, MultipartFile[] files)
            throws IOException, ResourceNotFoundException {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResourceNotFoundException("Notizia non trovata con ID: " + newsId));

        List<String> photos = new ArrayList<>(
                news.getPhotos() != null ? Arrays.asList(news.getPhotos()) : List.of());

        // Trova il prossimo progressivo disponibile
        int nextIndex = 1;
        Pattern pattern = Pattern.compile("gallery_" + newsId + "_(\\d+)\\.[a-z]+$");
        for (String photo : photos) {
            Matcher matcher = pattern.matcher(photo);
            if (matcher.find()) {
                int idx = Integer.parseInt(matcher.group(1));
                if (idx >= nextIndex)
                    nextIndex = idx + 1;
            }
        }

        for (MultipartFile file : files) {
            String path = photoService.uploadPhoto(EntityType.NEWS, newsId, file, PhotoType.GALLERY, nextIndex);
            photos.add(path);
            nextIndex++;
        }

        news.setPhotos(photos.toArray(new String[0]));
        newsRepository.save(news);
        return news;
    }

    @Transactional
    public News deleteGallery(String newsId, String photoName) throws IOException, ResourceNotFoundException {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResourceNotFoundException("Notizia non trovata con ID: " + newsId));
        String[] currentPhotos = news.getPhotos() != null ? news.getPhotos() : new String[0];
        List<String> photos = new ArrayList<>(Arrays.asList(currentPhotos));
        boolean removed = photos.removeIf(p -> p.endsWith(photoName)); // o usa equals se salvi solo il nome

        if (removed) {
            String uploadPath = photoService.getBaseUploadPath();
            photoService.deletePhoto(uploadPath + File.separator + EntityType.NEWS.name().toLowerCase()
                    + File.separator + newsId + File.separator + photoName);
            news.setPhotos(photos.toArray(new String[0]));
            newsRepository.save(news);
        }
        return news;
    }

}
