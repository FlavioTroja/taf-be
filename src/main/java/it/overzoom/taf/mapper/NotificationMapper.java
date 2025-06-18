package it.overzoom.taf.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import it.overzoom.taf.dto.NotificationDTO;
import it.overzoom.taf.model.Notification;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {
    NotificationDTO toDto(Notification entity);

    Notification toEntity(NotificationDTO dto);
}
