package it.overzoom.taf.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import it.overzoom.taf.model.Activity;

@Repository
public interface ActivityRepository extends MongoRepository<Activity, String> {

    List<Activity> findByMunicipalityId(String municipalityId);

    Page<Activity> findByMunicipalityIdIn(String[] municipalityIds, Pageable pageable);

    List<Activity> findByLatitudeBetweenAndLongitudeBetween(double south, double north, double west, double east);
}
