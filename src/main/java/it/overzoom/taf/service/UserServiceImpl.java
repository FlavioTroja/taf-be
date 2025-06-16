package it.overzoom.taf.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.model.User;
import it.overzoom.taf.repository.UserRepository;
import it.overzoom.taf.utils.SecurityUtils;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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

                    return existingUser;
                })
                .map(userRepository::save);
    }

    public boolean hasAccess(String userId) throws ResourceNotFoundException {
        return SecurityUtils.isAdmin() || SecurityUtils.isCurrentUser(userId);
    }
}
