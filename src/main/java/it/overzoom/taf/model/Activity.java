package it.overzoom.taf.model;

import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "activity")
public class Activity {

    @Id
    private String id;
    private String name;
    private String address;
    private String phone;
    private Binary[] photos;
    private Binary cover;
    private Binary logo;
    private String email;
    private String[] openingHours;
    private String website;
    private String description;
    private CategoryType category;
    private String[] tags;
    @Indexed
    private String municipalityId;

    public String getMunicipalityId() {
        return municipalityId;
    }

    public void setMunicipalityId(String municipalityId) {
        this.municipalityId = municipalityId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Binary[] getPhotos() {
        return photos;
    }

    public void setPhotos(Binary[] photos) {
        this.photos = photos;
    }

    public Binary getCover() {
        return cover;
    }

    public void setCover(Binary cover) {
        this.cover = cover;
    }

    public Binary getLogo() {
        return logo;
    }

    public void setLogo(Binary logo) {
        this.logo = logo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String[] getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String[] openingHours) {
        this.openingHours = openingHours;
    }

    public CategoryType getCategory() {
        return category;
    }

    public void setCategory(CategoryType category) {
        this.category = category;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
