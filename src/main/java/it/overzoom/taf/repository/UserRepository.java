package it.overzoom.taf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import it.overzoom.taf.model.User;
import it.overzoom.taf.type.NotificationType;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUserId(String userId);

    List<User> findByNotificationTypesContaining(NotificationType notificationType);
}
