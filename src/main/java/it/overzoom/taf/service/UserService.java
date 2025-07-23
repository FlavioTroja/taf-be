package it.overzoom.taf.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

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

    User uploadPhoto(String userId, MultipartFile file) throws IOException, ResourceNotFoundException;

    void deleteById(String id);

    void deleteByUserId(String userId);
}