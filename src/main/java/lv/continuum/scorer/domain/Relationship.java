package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.Translations;

import java.util.concurrent.atomic.AtomicInteger;

public class Relationship {

    private static final String CONCEPT_NEGATIVE_ID = Translations.getInstance().get("concept-negative-id");

    private static final AtomicInteger idIssuer = new AtomicInteger();

    private final int id;
    private final int fromConcept;
    private final int toConcept;
    private final String name;

    public Relationship(int fromConcept, int toConcept, String name) {
        this.id = idIssuer.getAndIncrement();

        if (fromConcept >= 0) this.fromConcept = fromConcept;
        else throw new UnsupportedOperationException(CONCEPT_NEGATIVE_ID);

        if (toConcept >= 0) this.toConcept = toConcept;
        else throw new UnsupportedOperationException(CONCEPT_NEGATIVE_ID);

        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public int getFromConcept() {
        return this.fromConcept;
    }

    public int getToConcept() {
        return this.toConcept;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        var name = getName() == null || getName().isEmpty() ?
                "Unmarked relationship with id " :
                "Relationship „" + getName() + "” with id ";
        return name + getId() +
                " from concept " + getFromConcept() +
                " to concept " + getToConcept();
    }
}
