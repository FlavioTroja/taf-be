package it.overzoom.taf.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class ConfigDTO extends BaseDTO {
    @NotNull(message = "Il comune non può essere nullo")
    @Schema(description = "ID del comune", example = "6852b4b11170095376c87d96")
    private String municipalityId;

    @Schema(description = "Contesto dell'applicazione o configurazione", example = "Gestione eventi")
    private String context;

    @Schema(description = "Sezione dell'applicazione o configurazione", example = "Amministrazione")
    private String section;

    @Schema(description = "Componente dell'applicazione o configurazione", example = "Modulo di pagamento")
    private String component;

    @Schema(description = "Stato attivo o disattivo della configurazione", example = "true")
    private Boolean isActive = true;

    @Schema(description = "Ruoli associati alla configurazione", example = "[\"ROLE_ADMIN\", \"ROLE_USER\"]")
    private String[] roles;

    public String getMunicipalityId() {
        return municipalityId;
    }

    public void setMunicipalityId(String municipalityId) {
        this.municipalityId = municipalityId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }
}
