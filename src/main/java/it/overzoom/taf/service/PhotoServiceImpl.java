package it.overzoom.taf.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import it.overzoom.taf.type.EntityType;
import it.overzoom.taf.type.PhotoType;

@Service
public class PhotoServiceImpl implements PhotoService {

    private static final Logger log = LoggerFactory.getLogger(PhotoServiceImpl.class);
    private final Environment environment;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    public PhotoServiceImpl(Environment environment) {
        this.environment = environment;
    }

    public String getBaseUploadPath() {
        return environment.getProperty("file-upload.path");
    }

    public String uploadPhoto(EntityType entityType, String entityId, MultipartFile file, PhotoType photoType)
            throws IOException {
        log.info("Avvio dell'upload per il file {} di tipo {} per l'entità {} con ID {}", file.getOriginalFilename(),
                photoType, entityType, entityId);
        return uploadPhoto(entityType, entityId, file, photoType, 0);
    }

    public String uploadPhoto(EntityType entityType, String entityId, MultipartFile file, PhotoType photoType,
            int index)
            throws IOException {
        log.info("Caricamento del file per l'entità {} con ID {}: tipo foto = {}, indice = {}", entityType, entityId,
                photoType, index);

        String uploadPath = getBaseUploadPath();
        String extension = getFileExtension(file);
        String filename = photoType.name().toLowerCase() + "_" + entityId + "_" + index + "." + extension;
        if (index == 0) {
            filename = photoType.name().toLowerCase() + "_" + entityId + "." + extension;
        }

        Path fullDir = Paths.get(uploadPath, entityType.name().toLowerCase(), entityId);
        Files.createDirectories(fullDir);

        Path fullPath = fullDir.resolve(filename);
        file.transferTo(fullPath.toFile());

        String relativePath = entityType.name().toLowerCase() + "/" + entityId + "/" + filename;
        log.info("Foto caricata con successo: {}", relativePath);
        return relativePath;
    }

    private String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            log.error("Nome del file non valido o senza estensione: {}", originalFilename);
            throw new IllegalArgumentException("File name is invalid or does not contain an extension");
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            log.error("Estensione del file non supportata: {}", ext);
            throw new IllegalArgumentException("Unsupported file extension: " + ext);
        }
        return ext;
    }

    public void deletePhoto(String photoPath) throws IOException {
        log.info("Tentativo di cancellazione della foto con percorso: {}", photoPath);
        Path path = Paths.get(photoPath);
        if (Files.deleteIfExists(path)) {
            log.info("Foto cancellata con successo: {}", photoPath);
        } else {
            log.warn("Impossibile cancellare la foto, il file non esiste: {}", photoPath);
        }
    }
}
