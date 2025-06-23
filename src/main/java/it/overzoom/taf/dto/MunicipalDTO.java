package it.overzoom.taf.dto;

import jakarta.validation.constraints.NotNull;

public class MunicipalDTO extends BaseDTO {

    @NotNull(message = "Il comune non può essere creato senza una città")
    private String city;
    @NotNull(message = "Il comune non può essere creato senza una provincia")
    private String province;
    private String region;
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
