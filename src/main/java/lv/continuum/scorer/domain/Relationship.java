package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.Translations;

/**
 * @author Andrey Pavlovich
 */
public class Relationship {
    private static int currentId = 0;
    private int id;
    private int fromConcept;
    private int toConcept;
    private String name;

    final public static String CONCEPT_NEGATIVE_ID = Translations.getInstance().get("concept-negative-id");

    public Relationship(int fromConcept, int toConcept) {
        this.setId();
        this.setFromConcept(fromConcept);
        this.setToConcept(toConcept);
    }

    public Relationship(int fromConcept, int toConcept, String name) {
        this(fromConcept, toConcept);
        this.setName(name);
    }

    private void setId() {
        this.id = currentId++;
    }

    public int getId() {
        return this.id;
    }

    final public void setFromConcept(int fromConcept) {
        if (fromConcept >= 0) this.fromConcept = fromConcept;
        else throw new UnsupportedOperationException(CONCEPT_NEGATIVE_ID);
    }

    public int getFromConcept() {
        return this.fromConcept;
    }

    final public void setToConcept(int toConcept) {
        if (toConcept >= 0) this.toConcept = toConcept;
        else throw new UnsupportedOperationException(CONCEPT_NEGATIVE_ID);
    }

    public int getToConcept() {
        return this.toConcept;
    }

    final public void setName(String name) {
        if (name != null && name.length() > 0) this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        if (this.getName() == null || this.getName().length() == 0)
            return "Unmarked relationship with id " +
                    this.getId() + " from concept " +
                    this.getFromConcept() + " to concept " +
                    this.getToConcept() + ".";
        return "Relationship „" +
                this.getName() + "” with id " +
                this.getId() + " from concept " +
                this.getFromConcept() + " to concept " +
                this.getToConcept() + ".";
    }
}
