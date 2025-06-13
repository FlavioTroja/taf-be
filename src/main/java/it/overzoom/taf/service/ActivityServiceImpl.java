package it.overzoom.taf.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.overzoom.taf.model.Activity;
import it.overzoom.taf.repository.ActivityRepository;

@Service
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityServiceImpl(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
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
            existing.setCategory(activity.getCategory());
            existing.setTags(activity.getTags());
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
            if (activity.getCategory() != null)
                existing.setCategory(activity.getCategory());
            if (activity.getTags() != null)
                existing.setTags(activity.getTags());
            return existing;
        }).map(activityRepository::save);
    }
}
