package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.InvalidDataException;
import lv.continuum.scorer.common.Translations;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// TODO Define equality and similarity
public class Concept {

    private static final Translations translations = Translations.getInstance();
    private static final List<String> lowerCaseNames = new CopyOnWriteArrayList<>();

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

    public Concept(String name) throws InvalidDataException {
        if (StringUtils.isEmpty(name)) {
            throw new InvalidDataException(CONCEPT_NO_NAME);
        } else {
            var lowerCaseName = name.toLowerCase();
            var id = lowerCaseNames.indexOf(lowerCaseName);
            if (id == -1) {
                lowerCaseNames.add(lowerCaseName);
                id = lowerCaseNames.indexOf(lowerCaseName);
            }
            this.id = id;
            this.name = name;
        }
    }

    @Override
    public String toString() {
        return "Concept „" + name + "” with id " + id;
    }
}
