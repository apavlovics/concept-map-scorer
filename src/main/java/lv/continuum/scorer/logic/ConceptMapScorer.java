package lv.continuum.scorer.logic;

import lv.continuum.scorer.common.InvalidDataException;
import lv.continuum.scorer.common.Translations;
import lv.continuum.scorer.domain.Concept;
import lv.continuum.scorer.domain.ConceptMap;
import lv.continuum.scorer.domain.Relationship;

import java.util.ArrayList;
import java.util.List;

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
            return this.countConceptMapElements(this.studentMap, translations.get("map-contains"));
        } else {
            return this.countConceptMapElements(
                    this.studentMap,
                    Translations.getInstance().get("student-map-contains")
            )
                    + "\n\n"
                    + this.countConceptMapElements(
                    this.teacherMap,
                    Translations.getInstance().get("teacher-map-contains")
            );
        }
    }

    public String compareConceptMapsUsingClosenessIndexes() {
        checkConceptMaps();
        var studentAllRelationships = studentMap.allRelationships();
        var teacherAllRelationships = teacherMap.allRelationships();

        double resultIndex = 0.0, closenessIndex;
        int sizeIntersection, sizeUnion;
        List<Double> closenessIndexes = new ArrayList<Double>();
        var similarConcepts = new ArrayList<Concept>();
        var temp = new ArrayList<Concept>();
        for (var sar : studentAllRelationships.entrySet()) {
            for (var tar : teacherAllRelationships.entrySet()) {
                if (sar.getKey().equals(tar.getKey())) {
                    similarConcepts.add(sar.getKey());
                    temp.clear();
                    temp.addAll(sar.getValue());

                    temp.retainAll(tar.getValue());
                    sizeIntersection = temp.size();

                    tar.getValue().removeAll(sar.getValue());
                    sar.getValue().addAll(tar.getValue());
                    sizeUnion = sar.getValue().size();

                    if (sizeIntersection == 0 && sizeUnion == 0) closenessIndex = 1;
                    else closenessIndex = (double) sizeIntersection / (double) sizeUnion;
                    closenessIndexes.add(closenessIndex);
                }
            }
        }
        for (var sc : similarConcepts) {
            studentAllRelationships.remove(sc);
            teacherAllRelationships.remove(sc);
        }
        for (var sar : studentAllRelationships.entrySet()) closenessIndexes.add(0.0);
        for (var par : teacherAllRelationships.entrySet()) closenessIndexes.add(0.0);
        for (Double ci : closenessIndexes) resultIndex += ci;
        resultIndex = resultIndex / closenessIndexes.size();
        return String.format(Translations.getInstance().get("maps-similarity-closeness-indexes"), resultIndex);
    }

    public String compareConceptMapsUsingImportanceIndexes() throws InvalidDataException {
        this.checkConceptMaps();
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

    public String compareConceptMapsUsingPropositionChains() {
        this.checkConceptMaps();
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

    public String compareConceptMapsUsingErrorAnalysis() {
        this.checkConceptMaps();
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

    private boolean similarConcepts() {
        checkConceptMaps();
        return studentMap.isSimilar(teacherMap);
    }

    private void checkConceptMaps() {
        if (this.teacherMap == null) {
            throw new UnsupportedOperationException(translations.get("no-teacher-map"));
        }
    }

    @Override
    public String toString() {
        return this.teacherMap != null ?
                "Student and teacher concept map scoring and comparison mode" :
                "Student concept map scoring mode";
    }
}
