package it.overzoom.taf.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.model.User;

public interface UserService {

    Page<User> findAll(Pageable pageable);

    Optional<User> findById(String userId);

    Optional<User> findByUserId(String userId);

    User getProfile() throws ResourceNotFoundException;

    boolean existsById(String id);

    User create(User user);

    Optional<User> update(User user);

    Optional<User> partialUpdate(String id, User user);

    boolean hasAccess(String userId) throws ResourceNotFoundException;
}