package it.overzoom.taf.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "municipal")
public class Municipal extends BaseEntity {

    private String name;
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
