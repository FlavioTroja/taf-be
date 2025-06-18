package it.overzoom.taf.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import it.overzoom.taf.dto.ConfigDTO;
import it.overzoom.taf.model.Config;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConfigMapper {

    ConfigDTO toDto(Config config);

    Config toEntity(ConfigDTO configDTO);
}
