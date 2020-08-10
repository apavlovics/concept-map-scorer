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

        double sumIntersection = 0, sumUnion = 0;
        for (var key : keyIntersection) {
            var studentKeyPaths = studentAllPaths.get(key);
            var teacherKeyPaths = teacherAllPaths.get(key);

            var intersection = new HashSet<>(studentKeyPaths);
            intersection.retainAll(teacherKeyPaths);
            sumIntersection += intersection.size();

            var union = new HashSet<>(studentKeyPaths);
            union.addAll(teacherKeyPaths);
            sumUnion += union.size();
        }
        studentAllPaths.keySet().removeAll(keyIntersection);
        teacherAllPaths.keySet().removeAll(keyIntersection);
        sumUnion += studentAllPaths.values().stream().mapToInt(Set::size).sum();
        sumUnion += teacherAllPaths.values().stream().mapToInt(Set::size).sum();
        var resultIndex = sumIntersection / sumUnion;
        return String.format(translations.get("maps-similarity-importance-indexes"), resultIndex);
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

        double teacherMapScore = 0, studentMapScore = 0, breakScore = 0;
        for (var tlp : teacherLongestPaths) {
            var tlpChainLength = tlp.size() - 1;
            teacherMapScore += tlpChainLength;

            double currentBreakScore = 0, approvedCurrentBreakScore = 0;
            for (var i = 0; i < tlpChainLength; i++) {
                if (studentMap.containsRelationship(tlp.get(i), tlp.get(i + 1))) {
                    studentMapScore++;
                    approvedCurrentBreakScore += currentBreakScore;
                    currentBreakScore = 0;
                } else {
                    currentBreakScore++;
                }
            }
            breakScore += approvedCurrentBreakScore / tlpChainLength;
        }
        var resultIndex =  (studentMapScore - breakScore) / teacherMapScore;
        return String.format(translations.get("maps-similarity-proposition-chains"), resultIndex);
    }

    public String compareConceptMapsUsingErrorAnalysis() throws InvalidDataException {
        this.checkTeacherConceptMap();
        if (!this.areConceptMapsSimilar())
            return translations.get("maps-different-concepts-error-analysis");

        var studentOutgoingRelationships = this.studentMap.outgoingRelationships();
        var teacherOutgoingRelationships = this.teacherMap.outgoingRelationships();

        double resultIndex, w1, w2, weightedResultIndex;
        int totalRelationships = (int) Math.pow(studentOutgoingRelationships.keySet().size(), 2);
        int correctRelationships = 0, incorrectRelationships = 0;

        for (var sor : studentOutgoingRelationships.entrySet()) {
            var studentRelationships = sor.getValue();
            var teacherRelationships = teacherOutgoingRelationships.get(sor.getKey());

            for (var sr : studentRelationships) {
                var relationshipFound = false;
                for (var tr : teacherRelationships)
                    if (sr.equals(tr)) {
                        correctRelationships++;
                        relationshipFound = true;
                    }
                if (!relationshipFound) incorrectRelationships++;
            }
        }

        var missingRelationships = this.teacherMap.relationshipCount() - correctRelationships;
        var noRelationships = totalRelationships - correctRelationships - incorrectRelationships - missingRelationships;

        resultIndex = (double) (correctRelationships
                + noRelationships
                - incorrectRelationships
                - missingRelationships) / (double) totalRelationships;
        w1 = (double) (correctRelationships + missingRelationships) / (double) (incorrectRelationships + noRelationships);
        w2 = 1.0 / w1;
        weightedResultIndex = (w2 * (double) correctRelationships
                + w1 * (double) noRelationships
                - w2 * (double) incorrectRelationships
                - w1 * (double) missingRelationships) / (double) totalRelationships;

        return String.format(translations.get("maps-similarity-error-analysis"), resultIndex)
                + String.format(translations.get("maps-similarity-error-analysis-weighted"), weightedResultIndex);
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
