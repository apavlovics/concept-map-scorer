package lv.continuum.scorer.logic;

import lv.continuum.scorer.common.InvalidDataException;
import lv.continuum.scorer.common.Translations;
import lv.continuum.scorer.domain.Concept;
import lv.continuum.scorer.domain.ConceptMap;
import lv.continuum.scorer.domain.Relationship;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.IntStream;

public class ConceptMapScorer {

    private static final Translations translations = Translations.getInstance();

    private final ConceptMap studentMap;
    private final ConceptMap teacherMap;

    public ConceptMapScorer(ConceptMap studentMap) throws InvalidDataException {
        this(studentMap, null);
    }

    public ConceptMapScorer(ConceptMap studentMap, ConceptMap teacherMap) throws InvalidDataException {
        if (studentMap == null) {
            throw new InvalidDataException(translations.get("no-student-map"));
        }
        this.studentMap = studentMap;
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

        var closenessIndexes = new ArrayList<Double>();
        var equalConcepts = new HashSet<Concept>();
        for (var sar : studentAllRelationships.entrySet()) {
            for (var tar : teacherAllRelationships.entrySet()) {
                if (sar.getKey().equals(tar.getKey())) {
                    equalConcepts.add(sar.getKey());

                    var intersection = new HashSet<>(sar.getValue());
                    intersection.retainAll(tar.getValue());
                    double intersectionCount = intersection.size();

                    var union = new HashSet<>(sar.getValue());
                    union.addAll(tar.getValue());
                    double unionCount = union.size();

                    var closenessIndex = intersectionCount == 0 && unionCount == 0 ? 1D : intersectionCount / unionCount;
                    closenessIndexes.add(closenessIndex);
                }
            }
        }
        studentAllRelationships.keySet().removeAll(equalConcepts);
        teacherAllRelationships.keySet().removeAll(equalConcepts);
        var differentConceptCount = studentAllRelationships.size() + teacherAllRelationships.size();
        IntStream.range(0, differentConceptCount).forEach(i -> closenessIndexes.add(0.0));

        var closenessIndexSum = closenessIndexes.stream().mapToDouble(Double::doubleValue).sum();
        var similarity = closenessIndexSum / closenessIndexes.size();
        return String.format(Translations.getInstance().get("maps-similarity-closeness-indexes"), similarity);
    }

    public String compareConceptMapsUsingImportanceIndexes() throws InvalidDataException {
        this.checkTeacherConceptMap();
        if (!this.similarConcepts())
            return Translations.getInstance().get("maps-different-concepts-importance-indexes");

        double resultIndex = 0.0;
        int sumIntersection = 0, sumUnion = 0;
        var studentAllPaths = this.studentMap.allPaths();
        var teacherAllPaths = this.teacherMap.allPaths();
        var similarRelationships = new ArrayList<Relationship>();
        var temp = new ArrayList<Relationship>();

        for (var sap : studentAllPaths.entrySet()) {
            for (var tap : teacherAllPaths.entrySet()) {
                if (sap.getKey().equals(tap.getKey())) {
                    similarRelationships.add(sap.getKey());
                    temp.clear();
                    temp.addAll(sap.getValue());

                    temp.retainAll(tap.getValue());
                    sumIntersection += temp.size();

                    tap.getValue().removeAll(sap.getValue());
                    sap.getValue().addAll(tap.getValue());
                    sumUnion += sap.getValue().size();
                }
            }
        }
        for (var sr : similarRelationships) {
            studentAllPaths.remove(sr);
            teacherAllPaths.remove(sr);
        }
        for (var sap : studentAllPaths.entrySet()) sumUnion += sap.getValue().size();
        for (var tap : teacherAllPaths.entrySet()) sumUnion += tap.getValue().size();
        resultIndex = (double) sumIntersection / (double) sumUnion;
        return String.format(Translations.getInstance().get("maps-similarity-importance-indexes"), resultIndex);
    }

    public String compareConceptMapsUsingPropositionChains() throws InvalidDataException {
        this.checkTeacherConceptMap();
        if (!this.similarConcepts())
            return Translations.getInstance().get("maps-different-concepts-proposition-chains");

        double resultIndex = 0.0;
        var studentLongestPaths = this.studentMap.longestPaths();
        var teacherLongestPaths = this.teacherMap.longestPaths();
        if (studentLongestPaths.isEmpty() || teacherLongestPaths.isEmpty())
            return Translations.getInstance().get("maps-cycles-proposition-chains");

        int teacherMapScore = 0, studentMapScore = 0;
        double breakScore = 0.0;
        int currentBreakScore, approvedCurrentBreakScore;
        for (var tlp : teacherLongestPaths) {
            teacherMapScore += tlp.size() - 1;
            currentBreakScore = 0;
            approvedCurrentBreakScore = 0;
            for (int i = 0; i < tlp.size() - 1; i++)
                if (this.studentMap.containsRelationship(tlp.get(i), tlp.get(i + 1))) {
                    studentMapScore++;
                    approvedCurrentBreakScore += currentBreakScore;
                    currentBreakScore = 0;
                } else currentBreakScore++;
            breakScore += (double) approvedCurrentBreakScore / (double) (tlp.size() - 1);
        }
        resultIndex = (double) (studentMapScore - breakScore) / (double) teacherMapScore;
        return String.format(Translations.getInstance().get("maps-similarity-proposition-chains"), resultIndex);
    }

    public String compareConceptMapsUsingErrorAnalysis() throws InvalidDataException {
        this.checkTeacherConceptMap();
        if (!this.similarConcepts())
            return Translations.getInstance().get("maps-different-concepts-error-analysis");

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

        return String.format(Translations.getInstance().get("maps-similarity-error-analysis"), resultIndex)
                + String.format(Translations.getInstance().get("maps-similarity-error-analysis-weighted"), weightedResultIndex);
    }

    private String countConceptMapElements(ConceptMap map, String title) {
        String returnString = (title == null) ? Translations.getInstance().get("map-contains") : title;
        long value;
        String format;
        value = map.conceptCount();
        format = Translations.getInstance().get("concepts");
        if (value == 0) format = Translations.getInstance().get("concepts-0");
        if (value == 1) format = Translations.getInstance().get("concepts-1");
        format += "\n";
        returnString += String.format(format, value);

        value = map.relationshipCount();
        format = Translations.getInstance().get("relationships");
        if (value == 0) format = Translations.getInstance().get("relationships-0");
        if (value == 1) format = Translations.getInstance().get("relationships-1");
        format += "\n";
        returnString += String.format(format, value);

        value = map.levelCount();
        format = Translations.getInstance().get("levels");
        if (value == 0) format = Translations.getInstance().get("levels-0");
        if (value == 1) format = Translations.getInstance().get("levels-1");
        format += "\n";
        returnString += String.format(format, value);

        value = map.branchCount();
        format = Translations.getInstance().get("branches");
        if (value == 0) format = Translations.getInstance().get("branches-0");
        if (value == 1) format = Translations.getInstance().get("branches-1");
        format += "\n";
        returnString += String.format(format, value);

        value = map.exampleCount();
        format = Translations.getInstance().get("examples");
        if (value == 0) format = Translations.getInstance().get("examples-0");
        if (value == 1) format = Translations.getInstance().get("examples-1");
        format += "\n";
        returnString += String.format(format, value);

        value = map.cycleCount();
        format = Translations.getInstance().get("cycles");
        if (value == 0) format = Translations.getInstance().get("cycles-0");
        if (value == 1) format = Translations.getInstance().get("cycles-1");
        format += "\n";
        returnString += String.format(format, value);

        value = map.subnetCount();
        format = Translations.getInstance().get("subnets");
        if (value == 0) format = Translations.getInstance().get("subnets-0");
        if (value == 1) format = Translations.getInstance().get("subnets-1");
        returnString += String.format(format, value);
        return returnString;
    }

    private boolean similarConcepts() throws InvalidDataException {
        checkTeacherConceptMap();
        return studentMap.isSimilar(teacherMap);
    }

    private void checkTeacherConceptMap() throws InvalidDataException {
        if (teacherMap == null) {
            throw new InvalidDataException(translations.get("no-teacher-map"));
        }
    }

    @Override
    public String toString() {
        return this.teacherMap != null ?
                "Student and teacher concept map scoring and comparison mode" :
                "Student concept map scoring mode";
    }
}
