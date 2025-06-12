package it.overzoom.taf.mapper;

import java.sql.Blob;
import java.util.Base64;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.web.bind.annotation.Mapping;

import it.overzoom.taf.dto.UserDTO;
import it.overzoom.taf.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "photo", target = "photo", qualifiedByName = "blobToBase64")
    UserDTO toDto(User user);

    @Mapping(source = "photo", target = "photo", qualifiedByName = "stringToBlob")
    User toEntity(UserDTO dto);

    @Named("blobToBase64")
    default String blobToBase64(Blob blob) {
        if (blob == null)
            return null;
        try {
            int length = (int) blob.length();
            byte[] bytes = blob.getBytes(1, length);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    @Named("stringToBlob")
    default Blob stringToBlob(String base64) {
        if (base64 == null)
            return null;
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            return new SerialBlob(bytes);
        } catch (Exception e) {
            return null;
        }
    }
}
