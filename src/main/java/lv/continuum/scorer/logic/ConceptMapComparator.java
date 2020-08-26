package lv.continuum.scorer.logic;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lv.continuum.scorer.common.InvalidDataException;
import lv.continuum.scorer.common.InvalidDataException.ErrorCode;
import lv.continuum.scorer.domain.ConceptMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class ConceptMapComparator {

    @NonNull
    private final ConceptMap studentConceptMap;

    @NonNull
    private final ConceptMap teacherConceptMap;

    public double compareUsingClosenessIndexes() {
        var studentAllRelationships = new HashMap<>(studentConceptMap.allRelationships);
        var teacherAllRelationships = new HashMap<>(teacherConceptMap.allRelationships);

        var keyIntersection = new HashSet<>(studentAllRelationships.keySet());
        keyIntersection.retainAll(teacherAllRelationships.keySet());

        var closenessIndexes = new ArrayList<Double>();
        for (var key : keyIntersection) {
            var studentKeyRelationships = studentAllRelationships.get(key);
            var teacherKeyRelationships = teacherAllRelationships.get(key);

            var intersection = new HashSet<>(studentKeyRelationships);
            intersection.retainAll(teacherKeyRelationships);
            double intersectionCount = intersection.size();

            var union = new HashSet<>(studentKeyRelationships);
            union.addAll(teacherKeyRelationships);
            double unionCount = union.size();

            double closenessIndex = intersectionCount == 0 && unionCount == 0 ? 1 : intersectionCount / unionCount;
            closenessIndexes.add(closenessIndex);
        }
        studentAllRelationships.keySet().removeAll(keyIntersection);
        teacherAllRelationships.keySet().removeAll(keyIntersection);
        var differentConceptCount = studentAllRelationships.size() + teacherAllRelationships.size();
        IntStream.range(0, differentConceptCount).forEach(i -> closenessIndexes.add(0.0));

        var closenessIndexSum = closenessIndexes.stream().mapToDouble(Double::doubleValue).sum();
        return closenessIndexSum / closenessIndexes.size();
    }

    public double compareUsingImportanceIndexes() throws InvalidDataException {
        checkAreSimilar(ErrorCode.DIFFERENT_CONCEPTS_IMPORTANCE_INDEXES);

        var studentAllPaths = studentConceptMap.allPaths();
        var teacherAllPaths = teacherConceptMap.allPaths();

        var keyIntersection = new HashSet<>(studentAllPaths.keySet());
        keyIntersection.retainAll(teacherAllPaths.keySet());

        double intersectionCount = 0, unionCount = 0;
        for (var key : keyIntersection) {
            var studentKeyPaths = studentAllPaths.get(key);
            var teacherKeyPaths = teacherAllPaths.get(key);

            var intersection = new HashSet<>(studentKeyPaths);
            intersection.retainAll(teacherKeyPaths);
            intersectionCount += intersection.size();

            var union = new HashSet<>(studentKeyPaths);
            union.addAll(teacherKeyPaths);
            unionCount += union.size();
        }
        studentAllPaths.keySet().removeAll(keyIntersection);
        teacherAllPaths.keySet().removeAll(keyIntersection);
        unionCount += studentAllPaths.values().stream().mapToInt(Set::size).sum();
        unionCount += teacherAllPaths.values().stream().mapToInt(Set::size).sum();

        return intersectionCount / unionCount;
    }

    public double compareUsingPropositionChains() throws InvalidDataException {
        checkAreSimilar(ErrorCode.DIFFERENT_CONCEPTS_PROPOSITION_CHAINS);

        var studentLongestPaths = studentConceptMap.longestPaths();
        var teacherLongestPaths = teacherConceptMap.longestPaths();
        if (studentLongestPaths.isEmpty() || teacherLongestPaths.isEmpty()) {
            throw new InvalidDataException(ErrorCode.CYCLES_PROPOSITION_CHAINS);
        }

        double teacherScore = 0, studentScore = 0, breakScore = 0;
        for (var tlp : teacherLongestPaths) {
            var tlpChainLength = tlp.size() - 1;
            teacherScore += tlpChainLength;

            double currentBreakScore = 0, approvedCurrentBreakScore = 0;
            for (var i = 0; i < tlpChainLength; i++) {
                if (studentConceptMap.containsRelationship(tlp.get(i), tlp.get(i + 1))) {
                    studentScore++;
                    approvedCurrentBreakScore += currentBreakScore;
                    currentBreakScore = 0;
                } else {
                    currentBreakScore++;
                }
            }
            breakScore += approvedCurrentBreakScore / tlpChainLength;
        }
        return (studentScore - breakScore) / teacherScore;
    }

    public SimilarityDegrees compareUsingErrorAnalysis() throws InvalidDataException {
        checkAreSimilar(ErrorCode.DIFFERENT_CONCEPTS_ERROR_ANALYSIS);

        var studentOutgoingRelationships = studentConceptMap.outgoingRelationships;
        var teacherOutgoingRelationships = teacherConceptMap.outgoingRelationships;

        double totalRelationships = Math.pow(studentOutgoingRelationships.size(), 2);
        double correctRelationships = 0, incorrectRelationships = 0;
        for (var sor : studentOutgoingRelationships.entrySet()) {
            var sorValue = sor.getValue();
            var torValue = teacherOutgoingRelationships.get(sor.getKey());

            var intersection = new HashSet<>(sorValue);
            intersection.retainAll(torValue);
            correctRelationships += intersection.size();

            var difference = new HashSet<>(sorValue);
            difference.removeAll(torValue);
            incorrectRelationships += difference.size();
        }
        var missingRelationships = teacherConceptMap.relationshipCount() - correctRelationships;
        var noRelationships = totalRelationships - correctRelationships - incorrectRelationships - missingRelationships;

        var similarityDegree = (correctRelationships +
                noRelationships -
                incorrectRelationships -
                missingRelationships) / totalRelationships;
        var weight1 = (correctRelationships + missingRelationships) / (incorrectRelationships + noRelationships);
        var weight2 = 1D / weight1;
        var weightedSimilarityDegree = (weight2 * correctRelationships +
                weight1 * noRelationships -
                weight2 * incorrectRelationships -
                weight1 * missingRelationships) / totalRelationships;
        return new SimilarityDegrees(similarityDegree, weightedSimilarityDegree);
    }

    private void checkAreSimilar(ErrorCode errorCode) throws InvalidDataException {
        if (!studentConceptMap.isSimilar(teacherConceptMap)) {
            throw new InvalidDataException(errorCode);
        }
    }

    @Value
    public static class SimilarityDegrees {
        public double similarityDegree;
        public double weightedSimilarityDegree;
    }
}
