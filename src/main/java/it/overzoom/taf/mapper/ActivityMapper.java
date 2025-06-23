package it.overzoom.taf.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import it.overzoom.taf.dto.ActivityDTO;
import it.overzoom.taf.model.Activity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ActivityMapper {

    ActivityDTO toDto(Activity entity);

    Activity toEntity(ActivityDTO dto);

}
