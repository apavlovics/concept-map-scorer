package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.Translations;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Concept {

    private static final String CONCEPT_NO_NAME = Translations.getInstance().get("concept-no-name");

    private static final List<String> names = new CopyOnWriteArrayList<>();

    private final int id;
    private final String name;

    public Concept(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(CONCEPT_NO_NAME);
        } else {
            var lowerCaseName = name.toLowerCase();
            var index = names.indexOf(lowerCaseName);
            if (index == -1) {
                names.add(lowerCaseName);
                index = names.indexOf(lowerCaseName);
            }
            this.id = index;
            this.name = name;
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Concept „" + getName() + "” with id " + getId();
    }
}
