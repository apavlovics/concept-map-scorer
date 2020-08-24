package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.InvalidDataException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RelationshipTest {

    private final Concept from;
    private final Concept to;

    RelationshipTest() throws InvalidDataException {
        from = new Concept("From");
        to = new Concept("To");
    }

    @Test
    void constructValid() {
        new Relationship(from, to, "relates to");
        new Relationship(from, to);
    }

    @Test
    void constructInvalid() {
        assertThrows(NullPointerException.class, () -> new Relationship(null, to, "consists of"));
        assertThrows(NullPointerException.class, () -> new Relationship(from, null, "consists of"));
    }

    @Test
    void equals() {
        assertEquals(new Relationship(from, to), new Relationship(from, to, "is example of"));
        assertNotEquals(new Relationship(from, to, "belongs to"), new Relationship(to, from, "belongs to"));
    }
}
