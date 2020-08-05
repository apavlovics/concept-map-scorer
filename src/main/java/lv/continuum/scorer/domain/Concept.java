package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.Translations;

import java.util.ArrayList;
import java.util.List;

public class Concept {

    private static final Translations translations = Translations.getInstance();

    // TODO Address thread safety concerns
    private static final List<String> names = new ArrayList<>();

    private final int id;
    private final String name;

    public Concept(String name) {
        if (name != null && name.length() > 0) {
            var lowerCaseName = name.toLowerCase();
            var index = names.indexOf(lowerCaseName);
            if (index == -1) {
                names.add(lowerCaseName);
                index = names.indexOf(lowerCaseName);
            }
            this.id = index;
            this.name = name;
        } else throw new UnsupportedOperationException(translations.get("concept-no-name"));
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "Concept „" + getName() + "” with id " + getId() + ".";
    }
}
