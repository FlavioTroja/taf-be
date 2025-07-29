package it.overzoom.taf.type;

public enum ActivityType {
    FOOD("Ristorazione"),
    BAR("Bar"),
    WELLNESS("Benessere"),
    EDUCATION("Istruzione"),
    RETAIL("Commercio al dettaglio"),
    SERVICES("Servizi"),
    ART("Arte"),
    PHARMACY("Farmacia"),
    ACCOMMODATION("Alloggio"),
    SPORT("Sport"),
    PLAYGROUND("Area giochi"),
    NIGHTLIFE("Vita notturna"),
    OTHER("Altro");

    private final String label;

    ActivityType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
