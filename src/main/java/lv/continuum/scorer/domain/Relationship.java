package lv.continuum.scorer.domain;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode
public class Relationship {

    public final Concept fromConcept;
    public final Concept toConcept;

    @EqualsAndHashCode.Exclude
    public final String name;

    public Relationship(@NonNull Concept fromConcept, @NonNull Concept toConcept, String name) {
        this.fromConcept = fromConcept;
        this.toConcept = toConcept;
        this.name = StringUtils.isNotBlank(name) ? name.trim() : null;
    }

    public Relationship(Concept fromConcept, Concept toConcept) {
        this(fromConcept, toConcept, null);
    }

    public boolean matches(String regex) {
        return name != null && name.matches(regex);
    }

    @Override
    public String toString() {
        var relationship = name != null ? " -> " + name + " -> " : " -> ";
        return fromConcept + relationship + toConcept;
    }
}
