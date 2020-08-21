package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.InvalidDataException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ConceptMapTest {

    private final Set<Concept> concepts;
    private final Set<Relationship> relationships;
    private final String fileName;

    ConceptMapTest() throws InvalidDataException {
        var a = new Concept("A");
        var b = new Concept("B");
        var c = new Concept("C");
        var d = new Concept("D");
        var e = new Concept("E");
        concepts = Set.of(a, b, c, d, e);
        relationships = Set.of(
                new Relationship(a, a),
                new Relationship(a, b),
                new Relationship(b, c),
                new Relationship(c, a)
        );
        fileName = "concept-map.xml";
    }

    @Test
    void constructValid() throws InvalidDataException {
        new ConceptMap(concepts, relationships, fileName);
        new ConceptMap(concepts, relationships, null);
    }

    @Test
    void constructInvalid() {
        assertThrows(InvalidDataException.class, () -> new ConceptMap(concepts, Set.of(), fileName));
        assertThrows(InvalidDataException.class, () -> new ConceptMap(Set.of(), relationships, fileName));
    }
}
