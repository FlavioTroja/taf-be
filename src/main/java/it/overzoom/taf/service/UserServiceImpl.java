package it.overzoom.taf.service;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PhotoService photoService;

    public UserServiceImpl(UserRepository userRepository, PhotoService photoService) {
        this.userRepository = userRepository;
        this.photoService = photoService;
    }

    public Page<User> findAll(Pageable pageable) {
        log.info("Recupero la lista di utenti per la pagina {}", pageable);
        return userRepository.findAll(pageable);
    }

    public Optional<User> findById(String userId) {
        log.info("Ricerca dell'utente con ID {}", userId);
        return userRepository.findById(userId);
    }

    public Optional<User> findByUserId(String userId) {
        log.info("Ricerca dell'utente con UserId {}", userId);
        return userRepository.findByUserId(userId);
    }

    public User getProfile() throws ResourceNotFoundException {
        String currentUserId = SecurityUtils.getCurrentUserId();
        log.info("Recupero il profilo dell'utente con ID {}", currentUserId);
        return userRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato"));
    }

    public boolean existsById(String id) {
        log.info("Verifica se l'utente con ID {} esiste", id);
        return userRepository.existsById(id);
    }

    @Transactional
    public User create(User user) {
        log.info("Creazione di un nuovo utente con ID {}", user.getId());
        return userRepository.save(user);
    }

    @Transactional
    public Optional<User> update(User user) {
        log.info("Aggiornamento delle informazioni dell'utente con ID {}", user.getId());
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
        log.info("Aggiornamento parziale dell'utente con ID {}", id);
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
        boolean hasAccess = SecurityUtils.isAdmin() || SecurityUtils.isCurrentUser(userId);
        log.info("Verifica accesso per l'utente con ID {}: {}", userId, hasAccess);
        return hasAccess;
    }

    @Transactional
    public User uploadPhoto(String userId, MultipartFile file) throws IOException, ResourceNotFoundException {
        log.info("Caricamento della foto per l'utente con ID {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato con ID: " + userId));
        String path = photoService.uploadPhoto(EntityType.USER, userId, file, PhotoType.LOGO);

        user.setPhoto(path);

        userRepository.save(user);
        log.info("Foto caricata per l'utente con ID {}: {}", userId, path);
        return user;
    }

    @Transactional
    public void deleteById(String id) {
        log.info("Eliminazione dell'utente con ID {}", id);
        userRepository.deleteById(id);
    }

    @Transactional
    public void deleteByUserId(String userId) {
        log.info("Eliminazione dell'utente con UserId {}", userId);
        userRepository.findByUserId(userId).ifPresent(user -> {
            userRepository.delete(user);
            log.info("Utente con UserId {} eliminato con successo", userId);
        });
    }
}
