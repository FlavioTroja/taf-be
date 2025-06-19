package it.overzoom.taf.mapper;

import java.util.Base64;
import java.util.stream.Stream;

import org.bson.types.Binary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import it.overzoom.taf.dto.EventDTO;
import it.overzoom.taf.model.Event;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {

    @Mapping(source = "photos", target = "photos", qualifiedByName = "binaryArrayToBase64Array")
    EventDTO toDto(Event entity);

    @Mapping(source = "photos", target = "photos", qualifiedByName = "base64ArrayToBinaryArray")
    Event toEntity(EventDTO dto);

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
                ? Stream.of(binaries).map(EventMapper::binaryToBase64).toArray(String[]::new)
                : null;
    }

    @Named("base64ArrayToBinaryArray")
    static Binary[] base64ArrayToBinaryArray(String[] base64Array) {
        return base64Array != null
                ? Stream.of(base64Array).map(EventMapper::base64ToBinary).toArray(Binary[]::new)
                : null;
    }
}
