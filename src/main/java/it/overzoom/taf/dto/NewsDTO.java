package it.overzoom.taf.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NewsDTO extends BaseDTO {
    @NotNull(message = "Il titolo non può essere nullo")
    @Schema(description = "Il titolo dell'articolo", example = "Novità sulla nuova riforma")
    private String title;

    @NotBlank(message = "Il contenuto non può essere vuoto")
    @Schema(description = "Contenuto dell'articolo", example = "La nuova riforma delle pensioni è stata approvata dal governo.")
    private String content;

    @Schema(description = "Autore dell'articolo", example = "Giorgio Rossi")
    private String author;

    @Schema(description = "Data di pubblicazione dell'articolo", example = "2025-06-30T12:00:00")
    private LocalDateTime publicationDate = LocalDateTime.now();

    @Schema(description = "Tag associati all'articolo", example = "[\"politica\", \"riforma\", \"pensioni\"]")
    private String[] tags;

    @Schema(description = "Path dell'immagine di copertura dell'articolo", example = "/images/cover.jpg")
    private String cover;

    @Schema(description = "Path delle foto associate all'articolo", example = "[\"/images/photo1.jpg\", \"/images/photo2.jpg\"]")
    private String[] photos;

    @NotNull(message = "Il comune non può essere nullo")
    @Schema(description = "ID del comune associato all'articolo", example = "6852b4b11170095376c87d96")
    private String municipalityId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDateTime publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String[] getPhotos() {
        return photos;
    }

    public void setPhotos(String[] photos) {
        this.photos = photos;
    }

    public String getMunicipalityId() {
        return municipalityId;
    }

    public void setMunicipalityId(String municipalityId) {
        this.municipalityId = municipalityId;
    }
}
