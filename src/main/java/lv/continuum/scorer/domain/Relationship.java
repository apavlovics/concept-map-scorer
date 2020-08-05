package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.Translations;

import java.util.concurrent.atomic.AtomicInteger;

public class Relationship {

    private static final String CONCEPT_NEGATIVE_ID = Translations.getInstance().get("concept-negative-id");

    private static final AtomicInteger idIssuer = new AtomicInteger();

    public final int id = idIssuer.getAndIncrement();
    public final int fromConcept;
    public final int toConcept;
    public final String name;

    public Relationship(int fromConcept, int toConcept, String name) {
        if (fromConcept < 0 || toConcept < 0) {
            throw new IllegalArgumentException(CONCEPT_NEGATIVE_ID);
        } else {
            this.fromConcept = fromConcept;
            this.toConcept = toConcept;
        }
        this.name = name;
    }

    @Override
    public String toString() {
        var prefix = name == null || name.isEmpty() ?
                "Unnamed relationship with id " :
                "Relationship „" + name + "” with id ";
        return prefix + id + " from concept " + fromConcept + " to concept " + toConcept;
    }
}
