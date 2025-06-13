package it.overzoom.taf.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import it.overzoom.taf.model.Activity;

public interface ActivityService {

    Page<Activity> findAll(Pageable pageable);

    Optional<Activity> findById(String id);

    boolean existsById(String id);

    Activity create(Activity activity);

    Optional<Activity> update(Activity activity);

    Optional<Activity> partialUpdate(String id, Activity activity);
}
