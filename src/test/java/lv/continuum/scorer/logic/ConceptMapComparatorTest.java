package lv.continuum.scorer.logic;

import lv.continuum.scorer.TestData;
import lv.continuum.scorer.common.InvalidDataException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConceptMapComparatorTest {

    private final TestData data = TestData.getInstance();
    private final ConceptMapComparator conceptMapComparator = new ConceptMapComparator(
            data.conceptMapSimilar1,
            data.conceptMapSimilar2
    );

    @Test
    void compareUsingClosenessIndexes() {
        assertEquals("0.786", conceptMapComparator.compareUsingClosenessIndexes());
    }

    @Test
    void compareUsingImportanceIndexes() throws InvalidDataException {
        assertEquals("0.381", conceptMapComparator.compareUsingImportanceIndexes());
    }

    @Test
    void compareUsingPropositionChains() throws InvalidDataException {
        assertEquals("0.667", conceptMapComparator.compareUsingPropositionChains());
    }

    @Test
    void compareUsingErrorAnalysis() throws InvalidDataException {
        var similarityDegrees = conceptMapComparator.compareUsingErrorAnalysis();
        assertEquals("0.837", similarityDegrees.similarityDegree);
        assertEquals("0.404", similarityDegrees.weightedSimilarityDegree);
    }

    private void assertEquals(String expected, double actual) {
        Assertions.assertEquals(expected, String.format("%.3f", actual));
    }
}
