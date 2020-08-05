package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.Translations;

import java.util.concurrent.atomic.AtomicInteger;

public class Relationship {

    private static final String CONCEPT_NEGATIVE_ID = Translations.getInstance().get("concept-negative-id");

    private static final AtomicInteger currentId = new AtomicInteger();

    private int id;
    private int fromConcept;
    private int toConcept;
    private String name;

    public Relationship(int fromConcept, int toConcept) {
        setId();
        setFromConcept(fromConcept);
        setToConcept(toConcept);
    }

    public Relationship(int fromConcept, int toConcept, String name) {
        this(fromConcept, toConcept);
        setName(name);
    }

    private void setId() {
        this.id = currentId.getAndIncrement();
    }

    public int getId() {
        return this.id;
    }

    public void setFromConcept(int fromConcept) {
        if (fromConcept >= 0) this.fromConcept = fromConcept;
        else throw new UnsupportedOperationException(CONCEPT_NEGATIVE_ID);
    }

    public int getFromConcept() {
        return this.fromConcept;
    }

    public void setToConcept(int toConcept) {
        if (toConcept >= 0) this.toConcept = toConcept;
        else throw new UnsupportedOperationException(CONCEPT_NEGATIVE_ID);
    }

    public int getToConcept() {
        return this.toConcept;
    }

    public void setName(String name) {
        if (name != null && name.length() > 0) this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        if (this.getName() == null || this.getName().length() == 0) {
            return "Unmarked relationship with id " +
                    getId() + " from concept " +
                    getFromConcept() + " to concept " +
                    getToConcept() + ".";
        } else {
            return "Relationship „" +
                    getName() + "” with id " +
                    getId() + " from concept " +
                    getFromConcept() + " to concept " +
                    getToConcept() + ".";
        }
    }
}
