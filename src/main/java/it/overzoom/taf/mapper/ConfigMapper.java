package it.overzoom.taf.mapper;

import org.mapstruct.Mapper;

import it.overzoom.taf.dto.ConfigDTO;

@Mapper(componentModel = "spring")
public interface ConfigMapper {

    ConfigDTO toDto(Config config);

    Config toEntity(ConfigDTO configDTO);
}
