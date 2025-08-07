package it.overzoom.taf.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.model.User;
import it.overzoom.taf.repository.UserRepository;
import it.overzoom.taf.type.EntityType;
import it.overzoom.taf.type.PhotoType;
import it.overzoom.taf.utils.SecurityUtils;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PhotoService photoService;

    public UserServiceImpl(UserRepository userRepository, PhotoService photoService) {
        this.userRepository = userRepository;
        this.photoService = photoService;
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
            existingUser.setMunicipalityIds(user.getMunicipalityIds());
            existingUser.setFcmToken(user.getFcmToken());
            existingUser.setNotificationTypes(user.getNotificationTypes());
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
                    if (user.getMunicipalityIds() != null) {
                        existingUser.setMunicipalityIds(user.getMunicipalityIds());
                    }
                    if (user.getVersion() != null) {
                        existingUser.setVersion(user.getVersion());
                    }
                    if (user.getUserId() != null) {
                        existingUser.setUserId(user.getUserId());
                    }
                    if (user.getFcmToken() != null) {
                        existingUser.setFcmToken(user.getFcmToken());
                    }
                    if (user.getNotificationTypes() != null) {
                        existingUser.setNotificationTypes(user.getNotificationTypes());
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
        String path = photoService.uploadPhoto(EntityType.USER, userId, file, PhotoType.LOGO);

        user.setPhoto(path);

        userRepository.save(user);
        return user;
    }

    @Transactional
    public void deleteById(String id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void deleteByUserId(String userId) {
        userRepository.findByUserId(userId).ifPresent(user -> {
            userRepository.delete(user);
        });
    }

}
