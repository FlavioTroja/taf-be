// Implementazione MunicipalServiceImpl
package it.overzoom.taf.service;

import java.io.IOException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import it.overzoom.taf.exception.ResourceNotFoundException;
import it.overzoom.taf.model.Municipal;
import it.overzoom.taf.repository.MunicipalRepository;
import it.overzoom.taf.type.EntityType;
import it.overzoom.taf.type.PhotoType;

@Service
public class MunicipalServiceImpl implements MunicipalService {

    private final MunicipalRepository municipalRepository;
    private final PhotoService photoService;

    public MunicipalServiceImpl(MunicipalRepository municipalRepository, PhotoService photoService) {
        this.municipalRepository = municipalRepository;
        this.photoService = photoService;
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
            existing.setDescription(municipal.getDescription());
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
            if (municipal.getDescription() != null) {
                existing.setDescription(municipal.getDescription());
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

    public Optional<Municipal> findByGeolocation(double latitude, double longitude) {
        return municipalRepository.findByCityAndProvince("Trani", "BT"); // Default city
    }

    @Transactional
    public Municipal uploadLogo(String id, MultipartFile file)
            throws IOException, ResourceNotFoundException {
        Municipal municipal = municipalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comune non trovato con ID: " + id));

        String path = photoService.uploadPhoto(EntityType.MUNICIPAL, id, file, PhotoType.LOGO);
        municipal.setLogo(path);
        municipalRepository.save(municipal);
        return municipal;
    }

    @Transactional
    public Municipal uploadCover(String id, MultipartFile file) throws IOException, ResourceNotFoundException {
        Municipal municipal = municipalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comune non trovato con ID: " + id));

        String path = photoService.uploadPhoto(EntityType.MUNICIPAL, id, file, PhotoType.COVER);
        municipal.setCover(path);
        municipalRepository.save(municipal);
        return municipal;
    }

    @Transactional
    public Municipal uploadIcon(String id, MultipartFile file) throws IOException, ResourceNotFoundException {
        Municipal municipal = municipalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comune non trovato con ID: " + id));

        String path = photoService.uploadPhoto(EntityType.MUNICIPAL, id, file, PhotoType.ICON);
        municipal.setIcon(path);
        municipalRepository.save(municipal);
        return municipal;
    }
}
