package it.overzoom.taf.mapper;

import java.util.Base64;
import java.util.stream.Stream;

import org.bson.types.Binary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import it.overzoom.taf.dto.ActivityDTO;
import it.overzoom.taf.model.Activity;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

    @Mapping(source = "cover", target = "cover", qualifiedByName = "binaryToBase64")
    @Mapping(source = "logo", target = "logo", qualifiedByName = "binaryToBase64")
    @Mapping(source = "photos", target = "photos", qualifiedByName = "binaryArrayToBase64Array")
    ActivityDTO toDto(Activity entity);

    @Mapping(source = "cover", target = "cover", qualifiedByName = "base64ToBinary")
    @Mapping(source = "logo", target = "logo", qualifiedByName = "base64ToBinary")
    @Mapping(source = "photos", target = "photos", qualifiedByName = "base64ArrayToBinaryArray")
    Activity toEntity(ActivityDTO dto);

    @Named("binaryToBase64")
    static String binaryToBase64(Binary binary) {
        return binary != null ? Base64.getEncoder().encodeToString(binary.getData()) : null;
    }

    @Named("base64ToBinary")
    static Binary base64ToBinary(String base64) {
        return base64 != null ? new Binary(Base64.getDecoder().decode(base64)) : null;
    }

    @Named("binaryArrayToBase64Array")
    static String[] binaryArrayToBase64Array(Binary[] binaries) {
        return binaries != null
                ? Stream.of(binaries).map(ActivityMapper::binaryToBase64).toArray(String[]::new)
                : null;
    }

    @Named("base64ArrayToBinaryArray")
    static Binary[] base64ArrayToBinaryArray(String[] base64Array) {
        return base64Array != null
                ? Stream.of(base64Array).map(ActivityMapper::base64ToBinary).toArray(Binary[]::new)
                : null;
    }
}
