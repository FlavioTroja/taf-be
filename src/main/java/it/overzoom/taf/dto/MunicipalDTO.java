package it.overzoom.taf.dto;

import jakarta.validation.constraints.NotNull;

public class MunicipalDTO extends BaseDTO {

    @NotNull
    private String name;
    @NotNull
    private String province;
    private String region;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
