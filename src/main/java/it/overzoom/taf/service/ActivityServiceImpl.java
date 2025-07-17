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
import it.overzoom.taf.model.Activity;
import it.overzoom.taf.repository.ActivityRepository;
import it.overzoom.taf.type.EntityType;
import it.overzoom.taf.type.PhotoType;

@Service
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final PhotoService photoService;

    public ActivityServiceImpl(ActivityRepository activityRepository, PhotoService photoService) {
        this.activityRepository = activityRepository;
        this.photoService = photoService;
    }

    @Override
    public Page<Activity> findAll(Pageable pageable) {
        return activityRepository.findAll(pageable);
    }

    @Override
    public Optional<Activity> findById(String id) {
        return activityRepository.findById(id);
    }

    @Override
    public boolean existsById(String id) {
        return activityRepository.existsById(id);
    }

    @Override
    @Transactional
    public Activity create(Activity activity) {
        return activityRepository.save(activity);
    }

    @Override
    @Transactional
    public Optional<Activity> update(Activity activity) {
        return activityRepository.findById(activity.getId()).map(existing -> {
            existing.setName(activity.getName());
            existing.setAddress(activity.getAddress());
            existing.setPhone(activity.getPhone());
            existing.setPhotos(activity.getPhotos());
            existing.setCover(activity.getCover());
            existing.setLogo(activity.getLogo());
            existing.setEmail(activity.getEmail());
            existing.setOpeningHours(activity.getOpeningHours());
            existing.setWebsite(activity.getWebsite());
            existing.setDescription(activity.getDescription());
            existing.setType(activity.getType());
            existing.setTags(activity.getTags());
            existing.setLatitude(activity.getLatitude());
            existing.setLongitude(activity.getLongitude());
            return existing;
        }).map(activityRepository::save);
    }

    @Override
    @Transactional
    public Optional<Activity> partialUpdate(String id, Activity activity) {
        return activityRepository.findById(id).map(existing -> {
            if (activity.getName() != null)
                existing.setName(activity.getName());
            if (activity.getAddress() != null)
                existing.setAddress(activity.getAddress());
            if (activity.getPhone() != null)
                existing.setPhone(activity.getPhone());
            if (activity.getPhotos() != null)
                existing.setPhotos(activity.getPhotos());
            if (activity.getCover() != null)
                existing.setCover(activity.getCover());
            if (activity.getLogo() != null)
                existing.setLogo(activity.getLogo());
            if (activity.getEmail() != null)
                existing.setEmail(activity.getEmail());
            if (activity.getOpeningHours() != null)
                existing.setOpeningHours(activity.getOpeningHours());
            if (activity.getWebsite() != null)
                existing.setWebsite(activity.getWebsite());
            if (activity.getDescription() != null)
                existing.setDescription(activity.getDescription());
            if (activity.getType() != null)
                existing.setType(activity.getType());
            if (activity.getTags() != null)
                existing.setTags(activity.getTags());
            if (activity.getLatitude() != null)
                existing.setLatitude(activity.getLatitude());
            if (activity.getLongitude() != null)
                existing.setLongitude(activity.getLongitude());
            return existing;
        }).map(activityRepository::save);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        activityRepository.deleteById(id);
    }

    @Transactional
    public Activity uploadLogo(String activityId, MultipartFile file) throws IOException, ResourceNotFoundException {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Attività non trovata con ID: " + activityId));

        String path = photoService.uploadPhoto(EntityType.ACTIVITY, activityId, file, PhotoType.LOGO);
        activity.setLogo(path);
        activityRepository.save(activity);
        return activity;
    }

    @Transactional
    public Activity uploadCover(String activityId, MultipartFile file) throws IOException, ResourceNotFoundException {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Attività non trovata con ID: " + activityId));

        String path = photoService.uploadPhoto(EntityType.ACTIVITY, activityId, file, PhotoType.COVER);
        activity.setCover(path);
        activityRepository.save(activity);
        return activity;
    }

    @Transactional
    public Activity uploadGallery(String activityId, MultipartFile[] files)
            throws IOException, ResourceNotFoundException {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Attività non trovata con ID: " + activityId));

        List<String> photos = new ArrayList<>(
                activity.getPhotos() != null ? Arrays.asList(activity.getPhotos()) : List.of());

        // Trova il prossimo progressivo disponibile
        int nextIndex = 1;
        Pattern pattern = Pattern.compile("gallery_" + activityId + "_(\\d+)\\.[a-z]+$");
        for (String photo : photos) {
            Matcher matcher = pattern.matcher(photo);
            if (matcher.find()) {
                int idx = Integer.parseInt(matcher.group(1));
                if (idx >= nextIndex)
                    nextIndex = idx + 1;
            }
        }

        for (MultipartFile file : files) {
            String path = photoService.uploadPhoto(EntityType.ACTIVITY, activityId, file, PhotoType.GALLERY, nextIndex);
            photos.add(path);
            nextIndex++;
        }

        activity.setPhotos(photos.toArray(new String[0]));
        activityRepository.save(activity);
        return activity;
    }

    @Transactional
    public Activity deleteGallery(String activityId, String photoName) throws IOException, ResourceNotFoundException {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Attività non trovata con ID: " + activityId));
        String[] currentPhotos = activity.getPhotos() != null ? activity.getPhotos() : new String[0];
        List<String> photos = new ArrayList<>(Arrays.asList(currentPhotos));
        boolean removed = photos.removeIf(p -> p.endsWith(photoName)); // o usa equals se salvi solo il nome

        if (removed) {
            String uploadPath = photoService.getBaseUploadPath();
            photoService.deletePhoto(uploadPath + File.separator + EntityType.ACTIVITY.name().toLowerCase()
                    + File.separator + activityId + File.separator + photoName);
            activity.setPhotos(photos.toArray(new String[0]));
            activityRepository.save(activity);
        }
        return activity;
    }
}
