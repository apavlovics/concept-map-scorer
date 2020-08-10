package lv.continuum.scorer.logic;

import lv.continuum.scorer.common.InvalidDataException;
import lv.continuum.scorer.common.Translations;
import lv.continuum.scorer.domain.ConceptMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

public class ConceptMapScorer {

    private static final Translations translations = Translations.getInstance();

    private final ConceptMap studentMap;
    private final ConceptMap teacherMap;

    public ConceptMapScorer(ConceptMap studentMap) {
        this(studentMap, null);
    }

    public ConceptMapScorer(ConceptMap studentMap, ConceptMap teacherMap) {
        this.studentMap = Objects.requireNonNull(studentMap);
        this.teacherMap = teacherMap;
    }

    public String countConceptMapsElements() {
        if (teacherMap == null) {
            return countConceptMapElements(studentMap, translations.get("map-contains"));
        } else {
            return countConceptMapElements(studentMap, translations.get("student-map-contains")) +
                    "\n\n" + countConceptMapElements(teacherMap, translations.get("teacher-map-contains"));
        }
    }

    public String compareConceptMapsUsingClosenessIndexes() throws InvalidDataException {
        checkTeacherConceptMap();
        var studentAllRelationships = studentMap.allRelationships();
        var teacherAllRelationships = teacherMap.allRelationships();

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
        var similarityDegree = closenessIndexSum / closenessIndexes.size();
        return String.format(translations.get("maps-similarity-closeness-indexes"), similarityDegree);
    }

    public String compareConceptMapsUsingImportanceIndexes() throws InvalidDataException {
        checkTeacherConceptMap();
        if (!areConceptMapsSimilar()) {
            return translations.get("maps-different-concepts-importance-indexes");
        }
        var studentAllPaths = studentMap.allPaths();
        var teacherAllPaths = teacherMap.allPaths();

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

        var similarityDegree = intersectionCount / unionCount;
        return String.format(translations.get("maps-similarity-importance-indexes"), similarityDegree);
    }

    public String compareConceptMapsUsingPropositionChains() throws InvalidDataException {
        checkTeacherConceptMap();
        if (!areConceptMapsSimilar()) {
            return translations.get("maps-different-concepts-proposition-chains");
        }
        var studentLongestPaths = studentMap.longestPaths();
        var teacherLongestPaths = teacherMap.longestPaths();
        if (studentLongestPaths.isEmpty() || teacherLongestPaths.isEmpty()) {
            return translations.get("maps-cycles-proposition-chains");
        }

        double teacherScore = 0, studentScore = 0, breakScore = 0;
        for (var tlp : teacherLongestPaths) {
            var tlpChainLength = tlp.size() - 1;
            teacherScore += tlpChainLength;

            double currentBreakScore = 0, approvedCurrentBreakScore = 0;
            for (var i = 0; i < tlpChainLength; i++) {
                if (studentMap.containsRelationship(tlp.get(i), tlp.get(i + 1))) {
                    studentScore++;
                    approvedCurrentBreakScore += currentBreakScore;
                    currentBreakScore = 0;
                } else {
                    currentBreakScore++;
                }
            }
            breakScore += approvedCurrentBreakScore / tlpChainLength;
        }
        var similarityDegree = (studentScore - breakScore) / teacherScore;
        return String.format(translations.get("maps-similarity-proposition-chains"), similarityDegree);
    }

    public String compareConceptMapsUsingErrorAnalysis() throws InvalidDataException {
        checkTeacherConceptMap();
        if (!areConceptMapsSimilar()) {
            return translations.get("maps-different-concepts-error-analysis");
        }

        var studentOutgoingRelationships = studentMap.outgoingRelationships();
        var teacherOutgoingRelationships = teacherMap.outgoingRelationships();

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
        var missingRelationships = teacherMap.relationshipCount() - correctRelationships;
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
        return String.format(translations.get("maps-similarity-error-analysis"), similarityDegree)
                + String.format(translations.get("maps-similarity-error-analysis-weighted"), weightedSimilarityDegree);
    }

    private String countConceptMapElements(ConceptMap conceptMap, String prefix) {
        return prefix +
                formatCount(conceptMap.conceptCount(), "concepts") + "\n" +
                formatCount(conceptMap.relationshipCount(), "relationships") + "\n" +
                formatCount(conceptMap.levelCount(), "levels") + "\n" +
                formatCount(conceptMap.branchCount(), "branches") + "\n" +
                formatCount(conceptMap.exampleCount(), "examples") + "\n" +
                formatCount(conceptMap.cycleCount(), "cycles") + "\n" +
                formatCount(conceptMap.subnetCount(), "subnets");
    }

    private String formatCount(long count, String keyPrefix) {
        var keySuffix = count == 0 ? "-0" : count == 1 ? "-1" : "";
        return String.format(translations.get(keyPrefix + keySuffix), count);
    }

    private boolean areConceptMapsSimilar() throws InvalidDataException {
        checkTeacherConceptMap();
        return studentMap.isSimilar(teacherMap);
    }

    private void checkTeacherConceptMap() throws InvalidDataException {
        if (teacherMap == null) {
            throw new InvalidDataException(translations.get("no-teacher-map"));
        }
    }
}
