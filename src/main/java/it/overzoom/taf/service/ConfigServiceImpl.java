package it.overzoom.taf.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.overzoom.taf.model.Config;
import it.overzoom.taf.repository.ConfigRepository;

@Service
public class ConfigServiceImpl implements ConfigService {

    private final ConfigRepository configRepository;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigServiceImpl.class);

    public ConfigServiceImpl(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    public Page<Config> findAll(Pageable pageable) {
        log.info("Fetching all config entries with pagination: {}", pageable);
        return configRepository.findAll(pageable);
    }

    @Override
    public Optional<Config> findById(String id) {
        log.info("Fetching config by ID: {}", id);
        return configRepository.findById(id);
    }

    @Override
    public boolean existsById(String id) {
        log.info("Checking if config exists by ID: {}", id);
        return configRepository.existsById(id);
    }

    @Override
    @Transactional
    public Config create(Config config) {
        log.info("Creating new config: {}", config);
        return configRepository.save(config);
    }

    @Override
    @Transactional
    public Optional<Config> update(Config config) {
        log.info("Updating config with ID: {}", config.getId());
        return configRepository.findById(config.getId()).map(existing -> {
            existing.setMunicipalityId(config.getMunicipalityId());
            existing.setContext(config.getContext());
            existing.setSection(config.getSection());
            existing.setComponent(config.getComponent());
            existing.setIsActive(config.getIsActive());
            existing.setRoles(config.getRoles());
            log.info("Updated config with ID: {}", existing.getId());
            return configRepository.save(existing);
        });
    }

    @Override
    @Transactional
    public Optional<Config> partialUpdate(String id, Config config) {
        log.info("Partially updating config with ID: {}", id);
        return configRepository.findById(id).map(existing -> {
            if (config.getMunicipalityId() != null) {
                existing.setMunicipalityId(config.getMunicipalityId());
            }
            if (config.getContext() != null) {
                existing.setContext(config.getContext());
            }
            if (config.getSection() != null) {
                existing.setSection(config.getSection());
            }
            if (config.getComponent() != null) {
                existing.setComponent(config.getComponent());
            }
            if (config.getIsActive() != null) {
                existing.setIsActive(config.getIsActive());
            }
            if (config.getRoles() != null) {
                existing.setRoles(config.getRoles());
            }
            log.info("Partially updated config with ID: {}", existing.getId());
            return configRepository.save(existing);
        });
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        log.info("Deleting config with ID: {}", id);
        configRepository.deleteById(id);
    }

    @Override
    public List<Config> findByRoles(String[] roles) {
        log.info("Fetching configs with roles: {}", (Object) roles);
        return configRepository.findByRolesIn(roles);
    }
}
