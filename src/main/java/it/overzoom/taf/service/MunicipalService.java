package it.overzoom.taf.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.model.Municipal;

public interface MunicipalService {

    Page<Municipal> findAll(Pageable pageable);

    Optional<Municipal> findById(String id);

    boolean existsById(String id);

    Municipal create(Municipal municipal);

    Optional<Municipal> update(Municipal municipal);

    Optional<Municipal> partialUpdate(String id, Municipal municipal);

    Optional<Municipal> findByDomain(String domain);

    Optional<Municipal> findByGeolocation(double latitude, double longitude);

    Optional<Municipal> getDefaultMunicipal();

    void deleteById(String id);

    Municipal uploadLogo(String id, MultipartFile file) throws IOException, ResourceNotFoundException;

    Municipal uploadCover(String id, MultipartFile file) throws IOException, ResourceNotFoundException;

    Municipal uploadIcon(String id, MultipartFile file) throws IOException, ResourceNotFoundException;
}
