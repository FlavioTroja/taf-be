package it.overzoom.taf.type;

public enum NotificationType {
    NEW_EVENTS("Nuovi eventi"),
    NEWS("Notizie"),
    SUBSCRIPTION_EVENTS("Eventi iscritti"),
    ACTIVITY_COMMUNICATIONS("Comunicazioni attività");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
