package it.overzoom.taf.type;

public enum NotificationType {
    new_events("Nuovi eventi"),
    news("Notizie"),
    subscription_events("Eventi iscritti"),
    activity_communications("Comunicazioni attività");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
