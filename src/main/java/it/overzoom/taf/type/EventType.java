package it.overzoom.taf.type;

public enum EventType {
    CONCERT("Concerto"),
    EXHIBITION("Mostra"),
    WORKSHOP("Workshop"),
    FESTIVAL("Festival"),
    CONFERENCE("Conferenza"),
    SPORT_EVENT("Evento sportivo"),
    THEATER("Teatro"),
    MOVIE_SCREENING("Proiezione film"),
    OTHER("Altro");

    private final String label;

    EventType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
