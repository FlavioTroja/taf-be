package it.overzoom.taf.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import it.overzoom.taf.type.ActivityTagType;
import it.overzoom.taf.type.ActivityType;
import jakarta.validation.constraints.NotNull;

public class ActivityDTO extends BaseDTO {

    @NotNull(message = "L'attività non può essere creata senza un nome")
    @Schema(description = "Il nome dell'attività", example = "Centro Sportivo")
    private String name;

    @Schema(description = "Indirizzo dell'attività", example = "Via Roma 1, Milano")
    private String address;

    @Schema(description = "Latitudine dell'attività (opzionale, per la visualizzazione sulla mappa)", example = "45.4642")
    private Double latitude;

    @Schema(description = "Longitudine dell'attività (opzionale, per la visualizzazione sulla mappa)", example = "9.1900")
    private Double longitude;

    @Schema(description = "Numero di telefono dell'attività", example = "+39 02 1234567")
    private String phone;

    @Schema(description = "Foto dell'attività (path alle foto)", example = "[\"/images/photo1.jpg\", \"/images/photo2.jpg\"]")
    private String[] photos;

    @Schema(description = "Path dell'immagine di copertura dell'attività", example = "/images/cover.jpg")
    private String cover;

    @Schema(description = "Path del logo dell'attività", example = "/images/logo.jpg")
    private String logo;

    @Schema(description = "Orari di apertura dell'attività", example = "[\"09:00-18:00\", \"09:00-13:00\"]")
    private String[] openingHours;

    @Schema(description = "Sito web dell'attività", example = "https://www.centrosportivo.com")
    private String website;

    @Schema(description = "Descrizione dell'attività", example = "Un centro sportivo che offre attività di tennis e nuoto.")
    private String description;

    @Schema(description = "Tipo di attività", example = "SPORT")
    private ActivityType type;

    @Schema(description = "Tag associati all'attività", example = "[\"tennis\", \"nuoto\", \"fitness\"]")
    private ActivityTagType[] tags;

    @NotNull(message = "L'attività non può essere creata senza un comune")
    @Schema(description = "ID del comune associato all'attività", example = "6852b4b11170095376c87d96")
    private String municipalityId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String[] getPhotos() {
        return photos;
    }

    public void setPhotos(String[] photos) {
        this.photos = photos;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String[] getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String[] openingHours) {
        this.openingHours = openingHours;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public ActivityTagType[] getTags() {
        return tags;
    }

    public void setTags(ActivityTagType[] tags) {
        this.tags = tags;
    }

    public String getMunicipalityId() {
        return municipalityId;
    }

    public void setMunicipalityId(String municipalityId) {
        this.municipalityId = municipalityId;
    }
}
