package it.overzoom.taf.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import it.overzoom.taf.model.Municipal;

public interface MunicipalService {

    Page<Municipal> findAll(Pageable pageable);

    Optional<Municipal> findById(String id);

    boolean existsById(String id);

    Municipal create(Municipal municipal);

    Optional<Municipal> update(Municipal municipal);

    Optional<Municipal> partialUpdate(String id, Municipal municipal);
}
