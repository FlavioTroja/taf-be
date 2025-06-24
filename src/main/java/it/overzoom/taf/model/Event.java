package it.overzoom.taf.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import it.overzoom.taf.type.EventType;

@Document(collection = "event")
public class Event extends BaseEntity {

    private String title;
    private String description;
    private EventType type;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String location;
    private String cover;
    private String[] photos;
    private String organizer;
    private String contactEmail;
    private String contactPhone;
    private String[] tags;
    @Indexed
    private String municipalityId;
    @Indexed
    private String activityId;
    private Integer maxParticipants;
    private Integer currentParticipants = 0; // default to 0, will be updated when participants register
    private Boolean isPublic = true; // default to public
    private Boolean isCancelled = false;
    private String url;
    private List<String> participants = new ArrayList<>(); // List of user IDs who registered for the event
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

    // Methods to handle participants
    public void addParticipant(String userId) {
        if (participants == null) {
            participants = new ArrayList<>();
        }

        if (maxParticipants == null || currentParticipants < maxParticipants) {
            if (!participants.contains(userId)) {
                participants.add(userId);
                currentParticipants++;
            }
        }
    }

    public void removeParticipant(String userId) {
        if (participants != null && participants.contains(userId)) {
            participants.remove(userId);
            currentParticipants--;
        }
    }

    public Map<String, LocalDateTime> getCheckInTimes() {
        return checkInTimes;
    }

    public void setCheckInTimes(Map<String, LocalDateTime> checkInTimes) {
        this.checkInTimes = checkInTimes;
    }

    // Metodo per aggiungere il check-in time
    public void addCheckIn(String userId) {
        checkInTimes.put(userId, LocalDateTime.now());
    }
}
