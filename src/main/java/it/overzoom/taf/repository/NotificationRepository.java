package it.overzoom.taf.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import it.overzoom.taf.model.Notification;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

}
