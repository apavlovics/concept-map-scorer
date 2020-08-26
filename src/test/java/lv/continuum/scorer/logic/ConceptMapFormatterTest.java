package lv.continuum.scorer.logic;

import lv.continuum.scorer.TestData;
import lv.continuum.scorer.common.InvalidDataException;
import lv.continuum.scorer.common.InvalidDataException.ErrorCode;
import lv.continuum.scorer.common.Translations;
import lv.continuum.scorer.logic.ConceptMapComparator.SimilarityDegrees;
import lv.continuum.scorer.logic.ConceptMapFormatter.Calculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConceptMapFormatterTest {

    private final TestData data = TestData.getInstance();

    private Translations translations;
    private ConceptMapFormatter conceptMapFormatter;

    @BeforeEach
    void beforeEach() {
        translations = mock(Translations.class);
        conceptMapFormatter = new ConceptMapFormatter(translations);
    }

    @Test
    void formatCounts() {
        var keyPrefix = "other";
        var prefix = "Other concept map contains:";
        var formattedConcepts = "some concepts;";
        var formattedRelationships = "some relationships;";
        var formattedLevels = "some levels;";
        var formattedBranches = "some branches;";
        var formattedExamples = "some examples;";
        var formattedCycles = "some cycles;";
        var formattedSubnets = "some subnets.";
        var delimiter = "\n";
        var formattedCounts = String.join(delimiter, List.of(
                prefix,
                formattedConcepts,
                formattedRelationships,
                formattedLevels,
                formattedBranches,
                formattedExamples,
                formattedCycles,
                formattedSubnets
        ));

        when(translations.get(keyPrefix + "-concept-map-contains")).thenReturn(prefix);
        when(translations.format("concepts", 3L)).thenReturn(formattedConcepts);
        when(translations.format("relationships-1", 1L)).thenReturn(formattedRelationships);
        when(translations.format("levels", 2L)).thenReturn(formattedLevels);
        when(translations.format("branches-0", 0L)).thenReturn(formattedBranches);
        when(translations.format("examples-0", 0L)).thenReturn(formattedExamples);
        when(translations.get("cycles-false")).thenReturn(formattedCycles);
        when(translations.format("subnets", 2L)).thenReturn(formattedSubnets);
        assertEquals(formattedCounts, conceptMapFormatter.formatCounts(keyPrefix, data.conceptMapOther));
    }

    @Test
    void formatSimilarityDegreeValid() {
        var similarityDegree = 9.8765D;
        var formattedSimilarityDegree = "Similarity degree of the maps using method";

        when(translations.format("similarity-method", similarityDegree)).thenReturn(formattedSimilarityDegree);
        assertEquals(formattedSimilarityDegree, conceptMapFormatter.formatSimilarityDegree("method", () -> similarityDegree));
    }

    @Test
    void formatSimilarityDegreeInvalid() {
        var errorCode = ErrorCode.DIFFERENT_CONCEPTS_IMPORTANCE_INDEXES;
        var formattedErrorCode = "Error code";

        when(translations.get(errorCode.translationKey)).thenReturn(formattedErrorCode);
        Calculator<Double> calculator = () -> {
            throw new InvalidDataException(errorCode);
        };
        assertEquals(formattedErrorCode, conceptMapFormatter.formatSimilarityDegree("method", calculator));
    }

    @Test
    void formatSimilarityDegreesValid() {
        var similarityDegrees = new SimilarityDegrees(1.2345D, 5.4321D);
        var formattedSimilarityDegree = "Similarity degree of the maps using method";
        var formattedWeightedSimilarityDegree = "Weighted similarity degree of the maps using method";
        var formattedSimilarityDegrees = formattedSimilarityDegree + "\n" + formattedWeightedSimilarityDegree;

        when(translations.format("similarity-method", similarityDegrees.similarityDegree))
                .thenReturn(formattedSimilarityDegree);
        when(translations.format("weighted-similarity-method", similarityDegrees.weightedSimilarityDegree))
                .thenReturn(formattedWeightedSimilarityDegree);
        assertEquals(formattedSimilarityDegrees, conceptMapFormatter.formatSimilarityDegrees("method", () -> similarityDegrees));
    }

    @Test
    void formatSimilarityDegreesInvalid() {
        var errorCode = ErrorCode.DIFFERENT_CONCEPTS_ERROR_ANALYSIS;
        var formattedErrorCode = "Error code";

        when(translations.get(errorCode.translationKey)).thenReturn(formattedErrorCode);
        Calculator<SimilarityDegrees> calculator = () -> {
            throw new InvalidDataException(errorCode);
        };
        assertEquals(formattedErrorCode, conceptMapFormatter.formatSimilarityDegrees("method", calculator));
    }
}
