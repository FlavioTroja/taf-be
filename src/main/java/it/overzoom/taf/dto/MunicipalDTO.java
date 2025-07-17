package it.overzoom.taf.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class MunicipalDTO extends BaseDTO {

    @NotNull(message = "Il comune non può essere creato senza una città")
    @Schema(description = "Nome della città del comune", example = "Trani")
    private String city;

    @NotNull(message = "Il comune non può essere creato senza una provincia")
    @Schema(description = "Nome della provincia del comune", example = "Barletta-Andria-Trani")
    private String province;

    @Schema(description = "Nome della regione del comune", example = "Puglia")
    private String region;

    @Schema(description = "Dominio associato al comune", example = "trani.autismfriendly.city")
    private String domain;

    @Schema(description = "Descrizione del comune", example = "Comune di Trani, città storica e turistica")
    private String description;

    @Schema(description = "Copertina del comune", example = "https://example.com/cover.jpg")
    private String cover;

    @Schema(description = "Logo del comune", example = "https://example.com/logo.png")
    private String logo;

    @Schema(description = "Icona del comune", example = "https://example.com/icon.png")
    private String icon;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

}
