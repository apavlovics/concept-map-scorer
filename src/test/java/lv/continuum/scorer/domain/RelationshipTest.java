package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.InvalidDataException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RelationshipTest {

    private final Concept from;
    private final Concept to;

    RelationshipTest() throws InvalidDataException {
        from = new Concept("From");
        to = new Concept("To");
    }

    @Test
    void constructValid() {
        new Relationship(from, to, "Name");
        new Relationship(from, to, null);
    }

    @Test
    void constructInvalid() {
        assertThrows(NullPointerException.class, () -> new Relationship(null, to, "Name"));
        assertThrows(NullPointerException.class, () -> new Relationship(from, null, "Name"));
    }
}
