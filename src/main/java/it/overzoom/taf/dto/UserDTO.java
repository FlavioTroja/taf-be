package it.overzoom.taf.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class UserDTO extends BaseDTO {

    @Schema(description = "ID univoco dell'utente in Cognito", example = "12345")
    private String userId;

    @NotNull(message = "Il nome non può essere nullo")
    @Schema(description = "Nome dell'utente", example = "Mario")
    private String name;

    @NotNull(message = "Il cognome non può essere nullo")
    @Schema(description = "Cognome dell'utente", example = "Rossi")
    private String surname;

    @Schema(description = "Data di nascita dell'utente", example = "1990-01-01")
    private LocalDate birthDate;

    @Schema(description = "Path alla foto dell'utente", example = "/images/user_photo.jpg")
    private String photo;

    @Schema(description = "Ruoli associati all'utente", example = "[\"ROLE_ADMIN\", \"ROLE_USER\"]")
    private String[] roles;

    @Schema(description = "ID del comune associato all'utente", example = "6852b4b11170095376c87d96")
    private String[] municipalityIds;

    @Schema(description = "Tipi di notifica a cui l'utente è iscritto", example = "[\"NEW_EVENTS\", \"NEWS\"]")
    private String[] notificationTypes;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String[] getMunicipalityIds() {
        return municipalityIds;
    }

    public void setMunicipalityIds(String[] municipalityIds) {
        this.municipalityIds = municipalityIds;
    }

    public String[] getNotificationTypes() {
        return notificationTypes;
    }

    public void setNotificationTypes(String[] notificationTypes) {
        this.notificationTypes = notificationTypes;
    }
}