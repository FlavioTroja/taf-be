package it.overzoom.taf.mapper;

import org.mapstruct.Mapper;

import it.overzoom.taf.dto.MunicipalDTO;
import it.overzoom.taf.model.Municipal;

@Mapper(componentModel = "spring")
public interface MunicipalMapper {

    MunicipalDTO toDto(Municipal entity);

    Municipal toEntity(MunicipalDTO dto);
}
