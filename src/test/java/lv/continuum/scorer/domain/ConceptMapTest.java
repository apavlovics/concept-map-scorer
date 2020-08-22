package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.InvalidDataException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConceptMapTest {

    private final Set<Concept> concepts;
    private final Set<Relationship> relationshipsWithLevels;
    private final Set<Relationship> relationshipsWithCycles;
    private final String fileName;
    private final ConceptMap conceptMapWithLevels;
    private final ConceptMap conceptMapWithCycles;

    ConceptMapTest() throws InvalidDataException {
        var a = new Concept("A");
        var b = new Concept("B");
        var c = new Concept("C");
        var d = new Concept("D");
        var e = new Concept("E");
        var f = new Concept("F");
        var g = new Concept("G");
        concepts = Set.of(a, b, c, d, e, f, g);
        relationshipsWithLevels = Set.of(
                new Relationship(a, b, "contains"),
                new Relationship(a, c, "is example of"),
                new Relationship(b, d, "includes"),
                new Relationship(c, e, "ir piemērs"),
                new Relationship(c, f),
                new Relationship(f, g)
        );
        relationshipsWithCycles = Set.of(
                new Relationship(a, a, "is instance of"),
                new Relationship(a, b, "ir eksemplārs"),
                new Relationship(b, b, "corresponds to"),
                new Relationship(b, c),
                new Relationship(c, a),
                new Relationship(d, e),
                new Relationship(e, f),
                new Relationship(f, e),
                new Relationship(f, g),
                new Relationship(g, e)
        );
        fileName = "concept-map.xml";
        conceptMapWithLevels = new ConceptMap(concepts, relationshipsWithLevels, fileName);
        conceptMapWithCycles = new ConceptMap(concepts, relationshipsWithCycles, fileName);
    }

    @Test
    void constructValid() throws InvalidDataException {
        new ConceptMap(concepts, relationshipsWithLevels, fileName);
        new ConceptMap(concepts, relationshipsWithCycles, null);
    }

    @Test
    void constructInvalid() {
        assertThrows(InvalidDataException.class, () -> new ConceptMap(concepts, Set.of(), fileName));
        assertThrows(InvalidDataException.class, () -> new ConceptMap(Set.of(), relationshipsWithCycles, fileName));
    }

    @Test
    void conceptCount() {
        assertEquals(7, conceptMapWithLevels.conceptCount());
        assertEquals(7, conceptMapWithCycles.conceptCount());
    }

    @Test
    void relationshipCount() {
        assertEquals(6, conceptMapWithLevels.relationshipCount());
        assertEquals(10, conceptMapWithCycles.relationshipCount());
    }

    @Test
    void levelCount() {
        assertEquals(4, conceptMapWithLevels.levelCount());
        assertEquals(0, conceptMapWithCycles.levelCount());
    }

    @Test
    void branchCount() {
        assertEquals(2, conceptMapWithLevels.branchCount());
        assertEquals(3, conceptMapWithCycles.branchCount());
    }

    @Test
    void exampleCount() {
        assertEquals(2, conceptMapWithLevels.exampleCount());
        assertEquals(2, conceptMapWithCycles.exampleCount());
    }

    @Test
    void cycleCount() {
        assertEquals(0, conceptMapWithLevels.cycleCount());
        assertEquals(5, conceptMapWithCycles.cycleCount());
    }

    @Test
    void subnetCount() {
        assertEquals(1, conceptMapWithLevels.subnetCount());
        assertEquals(2, conceptMapWithCycles.subnetCount());
    }
}
