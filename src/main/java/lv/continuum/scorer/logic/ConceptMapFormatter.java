package lv.continuum.scorer.logic;

import lombok.RequiredArgsConstructor;
import lv.continuum.scorer.common.InvalidDataException;
import lv.continuum.scorer.common.Translations;
import lv.continuum.scorer.domain.ConceptMap;
import lv.continuum.scorer.logic.ConceptMapComparator.SimilarityDegrees;

import java.util.List;

@RequiredArgsConstructor
public class ConceptMapFormatter {

    private final Translations translations;

    public String formatCounts(String keyPrefix, ConceptMap conceptMap) {
        var formattedCounts = List.of(
                formatCount("concepts", conceptMap.conceptCount()),
                formatCount("relationships", conceptMap.relationshipCount()),
                formatCount("levels", conceptMap.levelCount()),
                formatCount("branches", conceptMap.branchCount()),
                formatCount("examples", conceptMap.exampleCount()),
                formatCount("cycles", conceptMap.containsCycles()),
                formatCount("subnets", conceptMap.subnetCount())
        );
        return translations.get(keyPrefix + "-concept-map-contains") + "\n" + String.join("\n", formattedCounts);
    }

    private String formatCount(String keyPrefix, long value) {
        var keySuffix = value == 0 ? "-0" : value == 1 ? "-1" : "";
        return translations.format(keyPrefix + keySuffix, value);
    }

    private String formatCount(String keyPrefix, boolean value) {
        return translations.get(keyPrefix + "-" + value);
    }

    public interface Calculator<T> {
        T calculate() throws InvalidDataException;
    }

    public String formatSimilarityDegree(String key, Calculator<Double> calculator) {
        try {
            var similarityDegree = calculator.calculate();
            return translations.format("similarity-" + key, similarityDegree);
        } catch (InvalidDataException e) {
            return translations.get(e.errorCode.translationKey);
        }
    }

    public String formatSimilarityDegrees(String key, Calculator<SimilarityDegrees> calculator) {
        try {
            var similarityDegrees = calculator.calculate();
            return translations.format("similarity-" + key, similarityDegrees.similarityDegree) + "\n" +
                    translations.format("weighted-similarity-" + key, similarityDegrees.weightedSimilarityDegree);
        } catch (InvalidDataException e) {
            return translations.get(e.errorCode.translationKey);
        }
    }
}
