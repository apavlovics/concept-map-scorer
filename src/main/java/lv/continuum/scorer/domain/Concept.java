package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.InvalidDataException;
import lv.continuum.scorer.common.Translations;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class Concept {

    private static final Translations translations = Translations.getInstance();

    private static final String CONCEPT_NO_NAME = translations.get("concept-no-name");

    public final String id;
    public final String name;

    public Concept(String name) throws InvalidDataException {
        if (StringUtils.isBlank(name)) {
            throw new InvalidDataException(CONCEPT_NO_NAME);
        }
        this.id = deriveId(name);
        this.name = name;
    }

    public static String deriveId(String name) {
        return name.trim().toLowerCase().replace(' ', '-');
    }

    public boolean equals(String id) {
        return this.id.equals(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Concept concept = (Concept) o;
        return id.equals(concept.id);
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
