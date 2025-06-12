package it.overzoom.taf.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.overzoom.taf.model.User;
import it.overzoom.taf.repository.UserRepository;

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
        return userRepository.findByUserId(userId);
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
        return this.findById(user.getId()).map(existingUser -> {
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setPhoto(user.getPhoto());
            existingUser.setBirthDate(user.getBirthDate());
            return existingUser;
        }).map(this::create);
    }

    @Transactional
    public Optional<User> partialUpdate(String id, User user) {
        return this.findById(id)
                .map(existingUser -> {
                    if (user.getUserId() != null) {
                        existingUser.setUserId(user.getUserId());
                    }
                    if (user.getFirstName() != null) {
                        existingUser.setFirstName(user.getFirstName());
                    }
                    if (user.getLastName() != null) {
                        existingUser.setLastName(user.getLastName());
                    }
                    if (user.getPhoto() != null) {
                        existingUser.setPhoto(user.getPhoto());
                    }
                    if (user.getBirthDate() != null) {
                        existingUser.setBirthDate(user.getBirthDate());
                    }

                    return existingUser;
                })
                .map(this::create);
    }
}
