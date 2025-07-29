package it.overzoom.taf.model;

import java.time.LocalDate;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "user")
public class User extends BaseEntity {

    @Indexed
    private String userId;

    private String name;

    private String surname;

    private LocalDate birthDate;

    @Field("photo")
    private String photo;

    private String[] roles;

    @Indexed
    private String[] municipalityIds;

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String[] getMunicipalityIds() {
        return municipalityIds;
    }

    public void setMunicipalityIds(String[] municipalityIds) {
        this.municipalityIds = municipalityIds;
    }

}
