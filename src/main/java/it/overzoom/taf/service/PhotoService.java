package it.overzoom.taf.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import it.overzoom.taf.type.EntityType;
import it.overzoom.taf.type.PhotoType;

public interface PhotoService {

    String getBaseUploadPath();

    String uploadPhoto(EntityType entityType, String entityId, MultipartFile file, PhotoType photoType, int index)
            throws IOException;

    String uploadPhoto(EntityType entityType, String entityId, MultipartFile file, PhotoType photoType)
            throws IOException;

    void deletePhoto(String photoPath) throws IOException;
}
