package it.overzoom.taf.dto;

public class EnumDTO {
    private String value;
    private String label;

    public EnumDTO(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}