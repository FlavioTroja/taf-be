package it.overzoom.taf.mapper;

import java.util.Base64;

import org.bson.types.Binary;
import org.springframework.stereotype.Component;

import it.overzoom.taf.dto.EventDTO;
import it.overzoom.taf.model.Event;

@Component
public class EventMapper {

    public EventDTO toDto(Event event) {
        if (event == null)
            return null;

        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setType(event.getType());
        dto.setStartDateTime(event.getStartDateTime());
        dto.setEndDateTime(event.getEndDateTime());
        dto.setLocation(event.getLocation());
        dto.setPhotos(convertBinaryArrayToBase64(event.getPhotos()));
        dto.setOrganizer(event.getOrganizer());
        dto.setContactEmail(event.getContactEmail());
        dto.setContactPhone(event.getContactPhone());
        dto.setTags(event.getTags());
        dto.setMunicipalityId(event.getMunicipalityId());
        dto.setActivityId(event.getActivityId());
        dto.setMaxParticipants(event.getMaxParticipants());
        dto.setCurrentParticipants(event.getCurrentParticipants());
        dto.setIsPublic(event.getIsPublic());
        dto.setIsCancelled(event.getIsCancelled());
        dto.setUrl(event.getUrl());

        return dto;
    }

    public Event toEntity(EventDTO dto) {
        if (dto == null)
            return null;

        Event event = new Event();
        event.setId(dto.getId());
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setType(dto.getType());
        event.setStartDateTime(dto.getStartDateTime());
        event.setEndDateTime(dto.getEndDateTime());
        event.setLocation(dto.getLocation());
        event.setPhotos(convertBase64ArrayToBinary(dto.getPhotos()));
        event.setOrganizer(dto.getOrganizer());
        event.setContactEmail(dto.getContactEmail());
        event.setContactPhone(dto.getContactPhone());
        event.setTags(dto.getTags());
        event.setMunicipalityId(dto.getMunicipalityId());
        event.setActivityId(dto.getActivityId());
        event.setMaxParticipants(dto.getMaxParticipants());
        event.setCurrentParticipants(dto.getCurrentParticipants());
        event.setIsPublic(dto.getIsPublic());
        event.setIsCancelled(dto.getIsCancelled());
        event.setUrl(dto.getUrl());

        return event;
    }

    private String[] convertBinaryArrayToBase64(Binary[] binaries) {
        if (binaries == null)
            return null;
        String[] base64 = new String[binaries.length];
        for (int i = 0; i < binaries.length; i++) {
            base64[i] = Base64.getEncoder().encodeToString(binaries[i].getData());
        }
        return base64;
    }

    private Binary[] convertBase64ArrayToBinary(String[] base64Strings) {
        if (base64Strings == null)
            return null;
        Binary[] binaries = new Binary[base64Strings.length];
        for (int i = 0; i < base64Strings.length; i++) {
            binaries[i] = new Binary(Base64.getDecoder().decode(base64Strings[i]));
        }
        return binaries;
    }
}
