package it.overzoom.taf.type;

public enum ActivityTagType {
    ADJUSTABLE_LIGHTING("Illuminazione Regolabile"),
    NOISE_REDUCTION("Riduzione Rumori"),
    VISUAL_SIGNAGE("Segnaletica Visiva"),
    QUIET_ZONE("Zona Tranquilla"),
    ACCESSIBLE_PATHS("Percorsi Accessibili"),
    WAITING_AREA("Area d'Attesa"),
    SIMPLIFIED_MENU("Menu Semplificato"),
    ALTERNATIVE_COMMUNICATION("Comunicazione Alternativa"),
    TRAINED_STAFF("Staff Formato"),
    CALM_HOURS("Fascia Oraria Calma"),
    EXPERIENCE_CUSTOMIZATION("Personalizzazione Esperienza"),
    ACCESSIBLE_RESTROOMS("Servizi Igienici Accessibili"),
    SENSORY_MATERIALS("Materiali Sensoriali"),
    READABLE_LABELS("Etichette Leggibili"),
    PERSONALIZED_ASSISTANCE("Assistenza Personalizzata"),
    CLEAR_DESCRIPTION("Descrizione Chiara"),
    AUTISM_FRIENDLY_FAQ("FAQ Autism Friendly");

    private final String label;

    ActivityTagType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
