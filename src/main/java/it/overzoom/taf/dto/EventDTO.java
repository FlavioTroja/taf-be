package it.overzoom.taf.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.overzoom.taf.type.EventType;
import jakarta.validation.constraints.NotNull;

public class EventDTO extends BaseDTO {
    @NotNull(message = "Il titolo dell'evento non può essere vuoto")
    private String title;
    private String description;
    @NotNull(message = "Il tipo dell'evento non può essere vuoto")
    private EventType type;
    @NotNull(message = "La data di inizio non può essere vuota")
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String location;
    private String cover; // path to cover photo
    private String[] photos; // path to photos
    private String organizer;
    private String contactEmail;
    private String contactPhone;
    private String[] tags;
    @NotNull(message = "Il comune non può essere vuoto")
    private String municipalityId;
    private String activityId;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private Boolean isPublic;
    private Boolean isCancelled;
    private String url;
    private List<String> participants = new ArrayList<>();
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
