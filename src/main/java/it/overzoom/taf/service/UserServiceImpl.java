package it.overzoom.taf.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.model.User;
import it.overzoom.taf.repository.UserRepository;
import it.overzoom.taf.utils.SecurityUtils;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Environment environment;

    public UserServiceImpl(UserRepository userRepository, Environment environment) {
        this.userRepository = userRepository;
        this.environment = environment;
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> findByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }

    public User getProfile() throws ResourceNotFoundException {
        return userRepository.findByUserId(SecurityUtils.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));
    }

    public boolean existsById(String id) {
        return userRepository.existsById(id);
    }

    @Transactional
    public User create(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public Optional<User> update(User user) {
        return userRepository.findById(user.getId()).map(existingUser -> {
            existingUser.setName(user.getName());
            existingUser.setSurname(user.getSurname());
            existingUser.setBirthDate(user.getBirthDate());
            existingUser.setMunicipalityId(user.getMunicipalityId());
            return existingUser;
        }).map(userRepository::save);
    }

    @Transactional
    public Optional<User> partialUpdate(String id, User user) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    if (user.getName() != null) {
                        existingUser.setName(user.getName());
                    }
                    if (user.getSurname() != null) {
                        existingUser.setSurname(user.getSurname());
                    }
                    if (user.getBirthDate() != null) {
                        existingUser.setBirthDate(user.getBirthDate());
                    }
                    if (user.getMunicipalityId() != null) {
                        existingUser.setMunicipalityId(user.getMunicipalityId());
                    }
                    if (user.getVersion() != null) {
                        existingUser.setVersion(user.getVersion());
                    }
                    if (user.getUserId() != null) {
                        existingUser.setUserId(user.getUserId());
                    }

                    return existingUser;
                })
                .map(userRepository::save);
    }

    public boolean hasAccess(String userId) throws ResourceNotFoundException {
        return SecurityUtils.isAdmin() || SecurityUtils.isCurrentUser(userId);
    }

    @Transactional
    public User uploadPhoto(String userId, MultipartFile file) throws IOException, ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con ID: " + userId));
        String uploadPath = environment.getProperty("file-upload.path");
        Path targetLocation = Paths.get(
                uploadPath + "/photos",
                "profile_" + user.getId() + "." + getFileExtension(file));

        Files.createDirectories(targetLocation.getParent());

        if (Files.exists(targetLocation)) {
            Files.delete(targetLocation);
        }

        file.transferTo(targetLocation.toFile());

        user.setPhoto(targetLocation.getFileName().toString());

        userRepository.save(user);
        return user;
    }

    @Transactional
    public void deleteById(String id) {
        userRepository.deleteById(id);
    }

    private String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("File name is invalid or does not contain an extension");
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
    }

}
