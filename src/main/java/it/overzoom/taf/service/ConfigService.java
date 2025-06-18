package it.overzoom.taf.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import it.overzoom.taf.model.Config;

public interface ConfigService {

    Page<Config> findAll(Pageable pageable);

    Optional<Config> findById(String id);

    boolean existsById(String id);

    Config create(Config config);

    Optional<Config> update(Config config);

    Optional<Config> partialUpdate(String id, Config config);

    void deleteById(String id);

    List<Config> findByRoles(String[] roles);
}
