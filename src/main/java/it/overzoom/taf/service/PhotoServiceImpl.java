package it.overzoom.taf.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import it.overzoom.taf.type.EntityType;
import it.overzoom.taf.type.PhotoType;

@Service
public class PhotoServiceImpl implements PhotoService {

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
        return uploadPhoto(entityType, entityId, file, photoType, 0);
    }

    public String uploadPhoto(EntityType entityType, String entityId, MultipartFile file, PhotoType photoType,
            int index)
            throws IOException {
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
        return relativePath;
    }

    private String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("File name is invalid or does not contain an extension");
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Unsupported file extension: " + ext);
        }
        return ext;
    }

    public void deletePhoto(String photoPath) throws IOException {
        Files.deleteIfExists(Paths.get(photoPath));
    }
}
