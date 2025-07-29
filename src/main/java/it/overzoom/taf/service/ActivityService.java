package it.overzoom.taf.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.model.Activity;

public interface ActivityService {

    Page<Activity> findAll(Pageable pageable);

    Optional<Activity> findById(String id);

    boolean existsById(String id);

    Activity create(Activity activity);

    Optional<Activity> update(Activity activity);

    Optional<Activity> partialUpdate(String id, Activity activity);

    void deleteById(String id);

    Activity uploadLogo(String activityId, MultipartFile file) throws IOException, ResourceNotFoundException;

    Activity uploadCover(String activityId, MultipartFile file) throws IOException, ResourceNotFoundException;

    Activity uploadGallery(String activityId, MultipartFile[] files) throws IOException, ResourceNotFoundException;

    Activity deleteGallery(String activityId, String photoName) throws IOException, ResourceNotFoundException;

    Page<Activity> findByMunicipalityIdIn(String[] municipalityIds, Pageable pageable);
}
