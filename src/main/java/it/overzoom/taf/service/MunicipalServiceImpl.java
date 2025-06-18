// Implementazione MunicipalServiceImpl
package it.overzoom.taf.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.overzoom.taf.model.Municipal;
import it.overzoom.taf.repository.MunicipalRepository;

@Service
public class MunicipalServiceImpl implements MunicipalService {

    private final MunicipalRepository municipalRepository;

    public MunicipalServiceImpl(MunicipalRepository municipalRepository) {
        this.municipalRepository = municipalRepository;
    }

    public Page<Municipal> findAll(Pageable pageable) {
        return municipalRepository.findAll(pageable);
    }

    public Optional<Municipal> findById(String id) {
        return municipalRepository.findById(id);
    }

    public boolean existsById(String id) {
        return municipalRepository.existsById(id);
    }

    @Transactional
    public Municipal create(Municipal municipal) {
        return municipalRepository.save(municipal);
    }

    @Transactional
    public Optional<Municipal> update(Municipal municipal) {
        return municipalRepository.findById(municipal.getId()).map(existing -> {
            existing.setCity(municipal.getCity());
            existing.setProvince(municipal.getProvince());
            existing.setRegion(municipal.getRegion());
            existing.setDomain(municipal.getDomain());
            return existing;
        }).map(municipalRepository::save);
    }

    @Transactional
    public Optional<Municipal> partialUpdate(String id, Municipal municipal) {
        return municipalRepository.findById(id).map(existing -> {
            if (municipal.getCity() != null) {
                existing.setCity(municipal.getCity());
            }
            if (municipal.getProvince() != null) {
                existing.setProvince(municipal.getProvince());
            }
            if (municipal.getRegion() != null) {
                existing.setRegion(municipal.getRegion());
            }
            if (municipal.getDomain() != null) {
                existing.setDomain(municipal.getDomain());
            }
            return existing;
        }).map(municipalRepository::save);
    }

    // delete
    @Transactional
    public void deleteById(String id) {
        municipalRepository.deleteById(id);
    }

    public Optional<Municipal> findByDomain(String domain) {
        return municipalRepository.findByDomain(domain);
    }
}
