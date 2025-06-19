package it.overzoom.taf.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import it.overzoom.taf.dto.NewsDTO;
import it.overzoom.taf.model.News;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NewsMapper {

    NewsDTO toDto(News news);

    News toEntity(NewsDTO newsDTO);
}
