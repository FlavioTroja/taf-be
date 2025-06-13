package it.overzoom.taf.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import it.overzoom.taf.model.Activity;

@Repository
public interface ActivityRepository extends MongoRepository<Activity, String> {

    List<Activity> findByMunicipalityId(String municipalityId);
}
