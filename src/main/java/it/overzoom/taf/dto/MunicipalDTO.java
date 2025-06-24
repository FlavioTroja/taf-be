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
}
