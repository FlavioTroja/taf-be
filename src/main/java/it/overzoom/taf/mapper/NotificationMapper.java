package it.overzoom.taf.mapper;

import org.mapstruct.Mapper;

import it.overzoom.taf.dto.NotificationDTO;
import it.overzoom.taf.model.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationDTO toDto(Notification entity);

    Notification toEntity(NotificationDTO dto);
}
