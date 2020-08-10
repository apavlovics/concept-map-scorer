package lv.continuum.scorer.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class Relationship {

    public final Concept fromConcept;
    public final Concept toConcept;
    public final String name;

    public Relationship(Concept fromConcept, Concept toConcept) {
        this(fromConcept, toConcept, null);
    }

    public Relationship(Concept fromConcept, Concept toConcept, String name) {
        this.fromConcept = Objects.requireNonNull(fromConcept);
        this.toConcept = Objects.requireNonNull(toConcept);
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relationship that = (Relationship) o;
        return fromConcept.equals(that.fromConcept) && toConcept.equals(that.toConcept);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromConcept, toConcept);
    }

    @Override
    public String toString() {
        var relationship = StringUtils.isBlank(name) ? " -> " : " -> " + name + " -> ";
        return fromConcept + relationship + toConcept;
    }
}
