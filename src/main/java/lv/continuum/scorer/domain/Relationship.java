package lv.continuum.scorer.domain;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
@EqualsAndHashCode
public class Relationship {

    @NonNull
    public final Concept fromConcept;

    @NonNull
    public final Concept toConcept;

    @EqualsAndHashCode.Exclude
    public final String name;

    public Relationship(Concept fromConcept, Concept toConcept) {
        this(fromConcept, toConcept, null);
    }

    @Override
    public String toString() {
        var relationship = StringUtils.isBlank(name) ? " -> " : " -> " + name + " -> ";
        return fromConcept + relationship + toConcept;
    }
}
