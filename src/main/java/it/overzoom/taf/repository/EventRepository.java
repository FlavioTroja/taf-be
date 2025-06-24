package it.overzoom.taf.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import it.overzoom.taf.model.Event;

@Repository
public interface EventRepository extends MongoRepository<Event, String> {

    // cerca per municipalityId
    List<Event> findByMunicipalityId(String municipalityId);

    // cerca per activityId
    List<Event> findByActivityId(String activityId);

    @Query("{'participants': ?0}")
    Page<Event> findEventsByUserId(String userId, Pageable pageable);
}
