package lv.continuum.scorer.logic;

import lv.continuum.scorer.TestData;
import lv.continuum.scorer.common.InvalidDataException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ConceptMapComparatorTest {

    private final TestData data = TestData.getInstance();

    @Test
    void compareSimilar() throws InvalidDataException {
        var comparator = new ConceptMapComparator(data.conceptMapSimilar1, data.conceptMapSimilar2);
        assertEquals("0.786", comparator.compareUsingClosenessIndexes());
        assertEquals("0.381", comparator.compareUsingImportanceIndexes());
        assertEquals("0.667", comparator.compareUsingPropositionChains());

        var similarityDegrees = comparator.compareUsingErrorAnalysis();
        assertEquals("0.837", similarityDegrees.similarityDegree);
        assertEquals("0.404", similarityDegrees.weightedSimilarityDegree);
    }

    @Test
    void compareNotSimilar() {
        var comparator = new ConceptMapComparator(data.conceptMapWithLevels, data.conceptMapOther);
        assertEquals("0.143", comparator.compareUsingClosenessIndexes());
        assertThrows(InvalidDataException.class, comparator::compareUsingImportanceIndexes);
        assertThrows(InvalidDataException.class, comparator::compareUsingPropositionChains);
        assertThrows(InvalidDataException.class, comparator::compareUsingErrorAnalysis);
    }

    @Test
    void compareEqual() throws InvalidDataException {
        var comparator = new ConceptMapComparator(data.conceptMapWithLevels, data.conceptMapWithLevels);
        assertEquals("1.000", comparator.compareUsingClosenessIndexes());
        assertEquals("1.000", comparator.compareUsingImportanceIndexes());
        assertEquals("1.000", comparator.compareUsingPropositionChains());

        var similarityDegrees = comparator.compareUsingErrorAnalysis();
        assertEquals("1.000", similarityDegrees.similarityDegree);
        assertEquals("1.000", similarityDegrees.weightedSimilarityDegree);
    }

    private void assertEquals(String expected, double actual) {
        Assertions.assertEquals(expected, String.format("%.3f", actual));
    }
}
