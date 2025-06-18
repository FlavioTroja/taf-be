package it.overzoom.taf.dto;

import jakarta.validation.constraints.NotNull;

public class MunicipalDTO extends BaseDTO {

    @NotNull
    private String city;
    @NotNull
    private String province;
    private String region;

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
}
