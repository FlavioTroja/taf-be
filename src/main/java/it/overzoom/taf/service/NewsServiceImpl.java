package it.overzoom.taf.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import it.overzoom.taf.model.User;
import it.overzoom.taf.repository.NewsRepository;
import it.overzoom.taf.repository.UserRepository;
import it.overzoom.taf.type.EntityType;
import it.overzoom.taf.type.NotificationType;
import it.overzoom.taf.type.PhotoType;

@Service
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final PhotoService photoService;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(NewsServiceImpl.class);

    public NewsServiceImpl(NewsRepository newsRepository, NotificationService notificationService,
            UserRepository userRepository, PhotoService photoService) {
        this.newsRepository = newsRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.photoService = photoService;
    }

    @Override
    public Page<News> findAll(Pageable pageable) {
        log.info("Fetching all news with pagination: {}", pageable);
        return newsRepository.findAll(pageable);
    }

    @Override
    public Optional<News> findById(String id) {
        log.info("Fetching news by ID: {}", id);
        return newsRepository.findById(id);
    }

    @Override
    public boolean existsById(String id) {
        log.info("Checking if news exists by ID: {}", id);
        return newsRepository.existsById(id);
    }

    @Override
    @Transactional
    public News create(News news) {
        log.info("Creating news: {}", news);
        news = newsRepository.save(news);

        // Notifica push solo agli utenti iscritti
        List<User> subscribedUsers = userRepository.findByNotificationTypesContaining(NotificationType.NEWS);
        log.info("Sending notifications to subscribed users: {}", subscribedUsers.size());

        // invia notifica push a ciascun utente
        for (User user : subscribedUsers) {
            if (Arrays.asList(user.getMunicipalityIds()).contains(news.getMunicipalityId())) {
                notificationService.sendPushToUser(user.getId(), "Nuova notizia", news.getTitle(),
                        Map.of("newsId", news.getId(), "type", NotificationType.NEWS.name()));
                log.info("Notification sent to user {} for news ID {}", user.getId(), news.getId());
            }
        }

        return news;
    }

    @Override
    @Transactional
    public Optional<News> update(News news) {
        log.info("Updating news with ID: {}", news.getId());
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
        log.info("Partially updating news with ID: {}", id);
        return newsRepository.findById(id).map(existing -> {
            if (news.getTitle() != null) {
                log.debug("Updating title: {}", news.getTitle());
                existing.setTitle(news.getTitle());
            }
            if (news.getContent() != null) {
                log.debug("Updating content");
                existing.setContent(news.getContent());
            }
            if (news.getAuthor() != null) {
                log.debug("Updating author: {}", news.getAuthor());
                existing.setAuthor(news.getAuthor());
            }
            if (news.getPublicationDate() != null) {
                log.debug("Updating publication date: {}", news.getPublicationDate());
                existing.setPublicationDate(news.getPublicationDate());
            }
            if (news.getTags() != null) {
                log.debug("Updating tags: {}", news.getTags());
                existing.setTags(news.getTags());
            }
            return existing;
        }).map(newsRepository::save);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        log.info("Deleting news with ID: {}", id);
        newsRepository.deleteById(id);
    }

    @Transactional
    public News uploadCover(String newsId, MultipartFile file) throws IOException, ResourceNotFoundException {
        log.info("Uploading cover photo for news ID: {}", newsId);
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new ResourceNotFoundException("Notizia non trovata con ID: " + newsId));

        String path = photoService.uploadPhoto(EntityType.NEWS, newsId, file, PhotoType.COVER);
        news.setCover(path);
        newsRepository.save(news);
        log.info("Cover photo uploaded successfully for news ID: {}", newsId);
        return news;
    }

    @Transactional
    public News uploadGallery(String newsId, MultipartFile[] files)
            throws IOException, ResourceNotFoundException {
        log.info("Uploading gallery photos for news ID: {}", newsId);
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
        log.info("Gallery photos uploaded successfully for news ID: {}", newsId);
        return news;
    }

    @Transactional
    public News deleteGallery(String newsId, String photoName) throws IOException, ResourceNotFoundException {
        log.info("Deleting gallery photo for news ID: {} and photo: {}", newsId, photoName);
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
            log.info("Gallery photo deleted successfully for news ID: {} and photo: {}", newsId, photoName);
        } else {
            log.warn("Photo not found for deletion: {}", photoName);
        }
        return news;
    }
}
