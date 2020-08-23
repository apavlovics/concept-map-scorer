package lv.continuum.scorer.domain;

import lv.continuum.scorer.TestData;
import lv.continuum.scorer.common.InvalidDataException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConceptMapTest {

    private final TestData data = new TestData();

    ConceptMapTest() throws InvalidDataException {}

    @Test
    void constructValid() throws InvalidDataException {
        new ConceptMap(data.concepts, data.relationshipsWithLevels, data.fileName);
        new ConceptMap(data.concepts, data.relationshipsWithCycles, null);
    }

    @Test
    void constructInvalid() {
        assertThrows(InvalidDataException.class, () -> new ConceptMap(data.concepts, Set.of(), data.fileName));
        assertThrows(InvalidDataException.class, () -> new ConceptMap(Set.of(), data.relationshipsWithCycles, data.fileName));
    }

    @Test
    void conceptCount() {
        assertEquals(7, data.conceptMapWithLevels.conceptCount());
        assertEquals(7, data.conceptMapWithCycles.conceptCount());
        assertEquals(3, data.conceptMapOther.conceptCount());
    }

    @Test
    void relationshipCount() {
        assertEquals(6, data.conceptMapWithLevels.relationshipCount());
        assertEquals(10, data.conceptMapWithCycles.relationshipCount());
        assertEquals(1, data.conceptMapOther.relationshipCount());
    }

    @Test
    void levelCount() {
        assertEquals(4, data.conceptMapWithLevels.levelCount());
        assertEquals(0, data.conceptMapWithCycles.levelCount());
        assertEquals(2, data.conceptMapOther.levelCount());
    }

    @Test
    void branchCount() {
        assertEquals(2, data.conceptMapWithLevels.branchCount());
        assertEquals(3, data.conceptMapWithCycles.branchCount());
        assertEquals(0, data.conceptMapOther.branchCount());
    }

    @Test
    void exampleCount() {
        assertEquals(2, data.conceptMapWithLevels.exampleCount());
        assertEquals(2, data.conceptMapWithCycles.exampleCount());
        assertEquals(0, data.conceptMapOther.exampleCount());
    }

    @Test
    void cycleCount() {
        assertEquals(0, data.conceptMapWithLevels.cycleCount());
        assertEquals(5, data.conceptMapWithCycles.cycleCount());
        assertEquals(0, data.conceptMapOther.cycleCount());
    }

    @Test
    void subnetCount() {
        assertEquals(1, data.conceptMapWithLevels.subnetCount());
        assertEquals(2, data.conceptMapWithCycles.subnetCount());
        assertEquals(2, data.conceptMapOther.subnetCount());
    }

    @Test
    void isSimilar() {
        assertTrue(data.conceptMapWithLevels.isSimilar(data.conceptMapWithCycles));
        assertTrue(data.conceptMapWithCycles.isSimilar(data.conceptMapWithLevels));
        assertFalse(data.conceptMapWithLevels.isSimilar(data.conceptMapOther));
        assertFalse(data.conceptMapOther.isSimilar(data.conceptMapWithLevels));
    }

    @Test
    void containsRelationship() {
        assertTrue(data.conceptMapOther.containsRelationship(data.a, data.b));
        assertFalse(data.conceptMapOther.containsRelationship(data.a, data.c));
        assertFalse(data.conceptMapOther.containsRelationship(data.b, data.a));
    }

    @Test
    void outgoingRelationships() {
        assertEquals(data.outgoingRelationshipsWithLevels, data.conceptMapWithLevels.outgoingRelationships());
        // TODO Complete implementation
    }
}
