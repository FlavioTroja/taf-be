package it.overzoom.taf.mapper;

import java.util.Base64;

import org.bson.types.Binary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import it.overzoom.taf.dto.UserDTO;
import it.overzoom.taf.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "photo", target = "photo", qualifiedByName = "binaryToBase64")
    UserDTO toDto(User user);

    @Mapping(source = "photo", target = "photo", qualifiedByName = "base64ToBinary")
    User toEntity(UserDTO userDTO);

    @Named("binaryToBase64")
    default String binaryToBase64(Binary binary) {
        if (binary == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(binary.getData());
    }

    @Named("base64ToBinary")
    default Binary base64ToBinary(String base64) {
        if (base64 == null) {
            return null;
        }
        byte[] bytes = Base64.getDecoder().decode(base64);
        return new Binary(bytes);
    }
}
