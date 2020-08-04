package lv.continuum.scorer.domain;

import java.util.List;
import java.util.ArrayList;
import lv.continuum.scorer.common.TranslationDictionary;

/**
 * @author Andrey Pavlovich
 */
public class Concept {
    private static List<String> names = new ArrayList<String>();
    private int id;
    private String name;

    final public static String CONCEPT_NO_NAME = TranslationDictionary.getInstance().getTranslation("concept-no-name");

    public Concept(String name) {
        if (name != null && name.length() > 0) {
            if (names.contains(name.toLowerCase())) {
                this.id = names.indexOf(name.toLowerCase());
                this.name = name;
            }
            else {
                names.add(name.toLowerCase());
                this.id = names.indexOf(name.toLowerCase());
                this.name = name;
            }
        }
        else throw new UnsupportedOperationException(CONCEPT_NO_NAME);
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "Concept „" +
                this.getName() + "” with id " +
                this.getId() + ".";
    }
}
