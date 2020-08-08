package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.InvalidDataException;
import lv.continuum.scorer.common.Translations;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class Concept {

    private static final Translations translations = Translations.getInstance();

    private static final String CONCEPT_NEGATIVE_ID = translations.get("concept-negative-id");
    private static final String CONCEPT_NO_NAME = translations.get("concept-no-name");

    public final int id;
    public final String name;

    public Concept(int id, String name) throws InvalidDataException {
        if (id < 0) {
            throw new InvalidDataException(CONCEPT_NEGATIVE_ID);
        }
        if (StringUtils.isEmpty(name)) {
            throw new InvalidDataException(CONCEPT_NO_NAME);
        }
        this.id = id;
        this.name = name;
    }

    public boolean hasDuplicateName(String name) {
        return this.name.compareToIgnoreCase(name) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Concept concept = (Concept) o;
        return id == concept.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Concept „" + name + "” with id " + id;
    }
}
