package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.Translations;

import java.util.concurrent.atomic.AtomicInteger;

public class Relationship {

    private static final String CONCEPT_NEGATIVE_ID = Translations.getInstance().get("concept-negative-id");

    private static final AtomicInteger idIssuer = new AtomicInteger();

    private final int id = idIssuer.getAndIncrement();
    private final int fromConcept;
    private final int toConcept;
    private final String name;

    public Relationship(int fromConcept, int toConcept, String name) {
        if (fromConcept < 0 || toConcept < 0) {
            throw new IllegalArgumentException(CONCEPT_NEGATIVE_ID);
        } else {
            this.fromConcept = fromConcept;
            this.toConcept = toConcept;
        }
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public int getFromConcept() {
        return fromConcept;
    }

    public int getToConcept() {
        return toConcept;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        var name = getName() == null || getName().isEmpty() ?
                "Unnamed relationship with id " :
                "Relationship „" + getName() + "” with id ";
        return name + getId() +
                " from concept " + getFromConcept() +
                " to concept " + getToConcept();
    }
}
