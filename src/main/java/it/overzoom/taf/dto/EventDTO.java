package it.overzoom.taf.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import it.overzoom.taf.type.EventType;
import jakarta.validation.constraints.NotNull;

public class EventDTO extends BaseDTO {
    @NotNull(message = "Il titolo dell'evento non può essere vuoto")
    @Schema(description = "Il titolo dell'evento", example = "Concerto di Natale")
    private String title;

    @Schema(description = "Descrizione dell'evento", example = "Un grande concerto natalizio con musicisti e cori.")
    private String description;

    @NotNull(message = "Il tipo dell'evento non può essere vuoto")
    @Schema(description = "Tipo di evento", example = "CONCERTO")
    private EventType type;

    @NotNull(message = "La data di inizio non può essere vuota")
    @Schema(description = "Data e ora di inizio dell'evento", example = "2025-12-24T20:00:00")
    private LocalDateTime startDateTime;

    @Schema(description = "Data e ora di fine dell'evento", example = "2025-12-24T23:00:00")
    private LocalDateTime endDateTime;

    @Schema(description = "Posizione dell'evento", example = "Teatro alla Scala, Milano")
    private String location;

    @Schema(description = "Path dell'immagine di copertura dell'evento", example = "/images/cover.jpg")
    private String cover;

    @Schema(description = "Path delle foto dell'evento", example = "[\"/images/photo1.jpg\", \"/images/photo2.jpg\"]")
    private String[] photos;

    @Schema(description = "Organizzatore dell'evento", example = "Organizzazione Eventi Srl")
    private String organizer;

    @Schema(description = "Email di contatto per l'evento", example = "info@eventisrl.com")
    private String contactEmail;

    @Schema(description = "Numero di telefono di contatto per l'evento", example = "+39 02 1234567")
    private String contactPhone;

    @Schema(description = "Tag associati all'evento", example = "[\"musica\", \"natale\", \"concerto\"]")
    private String[] tags;

    @NotNull(message = "Il comune non può essere vuoto")
    @Schema(description = "ID del comune associato all'evento", example = "6852b4b11170095376c87d96")
    private String municipalityId;

    @Schema(description = "ID dell'attività associata all'evento", example = "12345678")
    private String activityId;

    @Schema(description = "Numero massimo di partecipanti all'evento", example = "500")
    private Integer maxParticipants;

    @Schema(description = "Numero attuale di partecipanti", example = "120")
    private Integer currentParticipants;

    @Schema(description = "Indica se l'evento è pubblico", example = "true")
    private Boolean isPublic;

    @Schema(description = "Indica se l'evento è cancellato", example = "false")
    private Boolean isCancelled;

    @Schema(description = "URL dell'evento", example = "https://www.evento.com")
    private String url;

    @Schema(description = "Lista dei partecipanti all'evento", example = "[\"user1\", \"user2\"]")
    private List<String> participants = new ArrayList<>();

    @Schema(description = "Orari di check-in dei partecipanti", example = "{\"user1\": \"2025-12-24T20:15:00\"}")
    private Map<String, LocalDateTime> checkInTimes = new HashMap<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String[] getPhotos() {
        return photos;
    }

    public void setPhotos(String[] photos) {
        this.photos = photos;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getMunicipalityId() {
        return municipalityId;
    }

    public void setMunicipalityId(String municipalityId) {
        this.municipalityId = municipalityId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Integer getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Boolean getIsCancelled() {
        return isCancelled;
    }

    public void setIsCancelled(Boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public Map<String, LocalDateTime> getCheckInTimes() {
        return checkInTimes;
    }

    public void setCheckInTimes(Map<String, LocalDateTime> checkInTimes) {
        this.checkInTimes = checkInTimes;
    }
}
