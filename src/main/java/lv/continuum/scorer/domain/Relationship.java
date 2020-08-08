package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.InvalidDataException;
import lv.continuum.scorer.common.Translations;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class Relationship {

    private static final Translations translations = Translations.getInstance();

    private static final String CONCEPT_NEGATIVE_ID = translations.get("concept-negative-id");

    public final int fromConcept;
    public final int toConcept;
    public final String name;

    public Relationship(int fromConcept, int toConcept, String name) throws InvalidDataException {
        if (fromConcept < 0 || toConcept < 0) {
            throw new InvalidDataException(CONCEPT_NEGATIVE_ID);
        } else {
            this.fromConcept = fromConcept;
            this.toConcept = toConcept;
        }
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relationship that = (Relationship) o;
        return fromConcept == that.fromConcept && toConcept == that.toConcept;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromConcept, toConcept);
    }

    @Override
    public String toString() {
        var prefix = StringUtils.isEmpty(name) ?
                "Unnamed relationship" :
                "Relationship „" + name + "”";
        return prefix + " from concept " + fromConcept + " to concept " + toConcept;
    }
}
