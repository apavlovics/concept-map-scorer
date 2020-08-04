package lv.continuum.scorer.logic;

import lv.continuum.scorer.domain.*;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import lv.continuum.scorer.common.TranslationDictionary;

/**
 * @author Andrey Pavlovich
 */
public class ConceptMapScorer {
    private ConceptMap studentMap;
    private ConceptMap teacherMap;

    final public static String NO_MAP = TranslationDictionary.getInstance().getTranslation("no-map");
    final public static String NO_STUDENT_MAP = TranslationDictionary.getInstance().getTranslation("no-student-map");
    final public static String NO_TEACHER_MAP = TranslationDictionary.getInstance().getTranslation("no-teacher-map");

    public ConceptMapScorer(ConceptMap studentMap) {
        this.setStudentMap(studentMap);
    }

    public ConceptMapScorer(ConceptMap studentMap, ConceptMap teacherMap) {
        this(studentMap);
        this.teacherMap = teacherMap;
    }

    private void setStudentMap(ConceptMap studentMap) {
        if (studentMap != null) this.studentMap = studentMap;
        else throw new UnsupportedOperationException(NO_STUDENT_MAP);
    }

    public String countConceptMapsElements() {
        if (this.teacherMap == null)
            return this.countConceptMapElements(this.studentMap, null);
        else
            return this.countConceptMapElements(
                    this.studentMap,
                    TranslationDictionary.getInstance().getTranslation("student-map-contains")
                    )
                + "\n\n"
                + this.countConceptMapElements(
                    this.teacherMap,
                    TranslationDictionary.getInstance().getTranslation("teacher-map-contains")
                    );
    }

    public String compareConceptMapsUsingClosenessIndexes() {
        this.checkConceptMaps();
        Map<Integer, List> studentAllRelationships = this.studentMap.allRelationships();
        Map<Integer, List> teacherAllRelationships = this.teacherMap.allRelationships();

        double resultIndex = 0.0, closenessIndex;
        int sizeIntersection, sizeUnion;
        List<Double> closenessIndexes = new ArrayList<Double>();
        List<Integer> similarConcepts = new ArrayList<Integer>();
        List<Integer> temp = new ArrayList<Integer>();
        for (Map.Entry<Integer, List> sar : studentAllRelationships.entrySet()) {
            for (Map.Entry<Integer, List> tar : teacherAllRelationships.entrySet()) {
                if (sar.getKey() == tar.getKey()) {
                    if (sar.getValue() == null) sar.setValue(new ArrayList<Integer>());
                    if (tar.getValue() == null) tar.setValue(new ArrayList<Integer>());
                    similarConcepts.add(sar.getKey());
                    temp.removeAll(temp);
                    temp.addAll(sar.getValue());

                    temp.retainAll(tar.getValue());
                    sizeIntersection = temp.size();

                    tar.getValue().removeAll(sar.getValue());
                    sar.getValue().addAll(tar.getValue());
                    sizeUnion = sar.getValue().size();

                    if (sizeIntersection == 0 && sizeUnion == 0) closenessIndex = 1;
                    else closenessIndex = (double)sizeIntersection / (double)sizeUnion;
                    closenessIndexes.add(closenessIndex);
                }
            }
        }
        for (Integer sc : similarConcepts) {
            studentAllRelationships.remove(sc);
            teacherAllRelationships.remove(sc);
        }
        for (Map.Entry<Integer, List> sar : studentAllRelationships.entrySet()) closenessIndexes.add(0.0);
        for (Map.Entry<Integer, List> par : teacherAllRelationships.entrySet()) closenessIndexes.add(0.0);
        for (Double ci : closenessIndexes) resultIndex += ci;
        resultIndex = resultIndex / closenessIndexes.size();
        return String.format(TranslationDictionary.getInstance().getTranslation("maps-similarity-closeness-indexes"), resultIndex);
    }

    public String compareConceptMapsUsingImportanceIndexes() {
        this.checkConceptMaps();
        if (!this.similarConcepts())
            return TranslationDictionary.getInstance().getTranslation("maps-different-concepts-importance-indexes");

        double resultIndex = 0.0;
        int sumIntersection = 0, sumUnion = 0;
        Map<String, List> studentAllPaths = this.studentMap.allPaths();
        Map<String, List> teacherAllPaths = this.teacherMap.allPaths();
        List<String> similarRelationships = new ArrayList<String>();
        List<String> temp = new ArrayList<String>();

        for (Map.Entry<String, List> sap : studentAllPaths.entrySet()) {
            for (Map.Entry<String, List> tap : teacherAllPaths.entrySet()) {
                if (sap.getKey().equals(tap.getKey())) {
                    similarRelationships.add(sap.getKey());
                    temp.removeAll(temp);
                    temp.addAll(sap.getValue());

                    temp.retainAll(tap.getValue());
                    sumIntersection += temp.size();

                    tap.getValue().removeAll(sap.getValue());
                    sap.getValue().addAll(tap.getValue());
                    sumUnion += sap.getValue().size();
                }
            }
        }
        for (String sr : similarRelationships) {
            studentAllPaths.remove(sr);
            teacherAllPaths.remove(sr);
        }
        for (Map.Entry<String, List> sap : studentAllPaths.entrySet()) sumUnion += sap.getValue().size();
        for (Map.Entry<String, List> tap : teacherAllPaths.entrySet()) sumUnion += tap.getValue().size();
        resultIndex = (double)sumIntersection / (double)sumUnion;
        return String.format(TranslationDictionary.getInstance().getTranslation("maps-similarity-importance-indexes"), resultIndex);
    }

    public String compareConceptMapsUsingPropositionChains() {
        this.checkConceptMaps();
        if (!this.similarConcepts())
            return TranslationDictionary.getInstance().getTranslation("maps-different-concepts-proposition-chains");

        double resultIndex = 0.0;
        List<ArrayList> studentLongestPaths = this.studentMap.longestPaths();
        List<ArrayList> teacherLongestPaths = this.teacherMap.longestPaths();
        if (studentLongestPaths == null || teacherLongestPaths == null)
            return TranslationDictionary.getInstance().getTranslation("maps-cycles-proposition-chains");

        int teacherMapScore = 0, studentMapScore = 0;
        double breakScore = 0.0;
        int currentBreakScore, approvedCurrentBreakScore;
        for (ArrayList<Integer> tlp : teacherLongestPaths) {
            teacherMapScore += tlp.size() - 1;
            currentBreakScore = 0;
            approvedCurrentBreakScore = 0;
            for (int i = 0; i < tlp.size() - 1; i++)
                if (this.studentMap.containsRelationship(tlp.get(i), tlp.get(i + 1))) {
                    studentMapScore++;
                    approvedCurrentBreakScore += currentBreakScore;
                    currentBreakScore = 0;
                }
                else currentBreakScore++;
            breakScore += (double)approvedCurrentBreakScore / (double)(tlp.size() - 1);
        }
        resultIndex = (double)(studentMapScore - breakScore) / (double)teacherMapScore;
        return String.format(TranslationDictionary.getInstance().getTranslation("maps-similarity-proposition-chains"), resultIndex);
    }

    public String compareConceptMapsUsingErrorAnalysis() {
        this.checkConceptMaps();
        if (!this.similarConcepts())
            return TranslationDictionary.getInstance().getTranslation("maps-different-concepts-error-analysis");

        Map<Integer, List> studentOutgoingRelationships = this.studentMap.outgoingRelationships();
        Map<Integer, List> teacherOutgoingRelationships = this.teacherMap.outgoingRelationships();

        double resultIndex, w1, w2, weightedResultIndex;
        int totalRelationships = (int)Math.pow(studentOutgoingRelationships.keySet().size(), 2);
        int correctRelationships = 0, incorrectRelationships = 0, missingRelationships = 0, noRelationships = 0;
        boolean relationshipFound;

        List<Integer> studentRelationships, teacherRelationships;
        for (Map.Entry<Integer, List> sor : studentOutgoingRelationships.entrySet()) {
            if (sor.getValue() == null) sor.setValue(new ArrayList<Integer>());
            if (teacherOutgoingRelationships.get(sor.getKey()) == null) teacherOutgoingRelationships.put(sor.getKey(), new ArrayList<Integer>());
            studentRelationships = sor.getValue();
            teacherRelationships = teacherOutgoingRelationships.get(sor.getKey());

            for (Integer sr : studentRelationships) {
                relationshipFound = false;
                for (Integer tr : teacherRelationships)
                    if (sr == tr) {
                        correctRelationships++;
                        relationshipFound = true;
                    }
                if (!relationshipFound) incorrectRelationships++;
            }
        }

        missingRelationships = this.teacherMap.relationshipCount() - correctRelationships;
        noRelationships = totalRelationships - correctRelationships - incorrectRelationships - missingRelationships;

        resultIndex = (double)(correctRelationships
                + noRelationships
                - incorrectRelationships
                - missingRelationships) / (double)totalRelationships;
        w1 = (double)(correctRelationships + missingRelationships) / (double)(incorrectRelationships + noRelationships);
        w2 = 1.0 / w1;
        weightedResultIndex = (w2 * (double)correctRelationships
                + w1 * (double)noRelationships
                - w2 * (double)incorrectRelationships
                - w1 * (double)missingRelationships) / (double)totalRelationships;

        return String.format(TranslationDictionary.getInstance().getTranslation("maps-similarity-error-analysis"), resultIndex)
                + String.format(TranslationDictionary.getInstance().getTranslation("maps-similarity-error-analysis-weighted"), weightedResultIndex);
    }

    private String countConceptMapElements(ConceptMap map, String title) {
        if (map == null) throw new UnsupportedOperationException(NO_MAP);

        String returnString = (title == null) ? TranslationDictionary.getInstance().getTranslation("map-contains") : title;
        int value;
        String format;
        value = map.conceptCount();
        format = TranslationDictionary.getInstance().getTranslation("concepts");
        if (value == 0) format = TranslationDictionary.getInstance().getTranslation("concepts-0");
        if (value == 1) format = TranslationDictionary.getInstance().getTranslation("concepts-1");
        format += "\n";
        returnString += String.format(format, value);

        value = map.relationshipCount();
        format = TranslationDictionary.getInstance().getTranslation("relationships");
        if (value == 0) format = TranslationDictionary.getInstance().getTranslation("relationships-0");
        if (value == 1) format = TranslationDictionary.getInstance().getTranslation("relationships-1");
        format += "\n";
        returnString += String.format(format, value);

        value = map.levelCount();
        format = TranslationDictionary.getInstance().getTranslation("levels");
        if (value == 0) format = TranslationDictionary.getInstance().getTranslation("levels-0");
        if (value == 1) format = TranslationDictionary.getInstance().getTranslation("levels-1");
        format += "\n";
        returnString += String.format(format, value);

        value = map.branchCount();
        format = TranslationDictionary.getInstance().getTranslation("branches");
        if (value == 0) format = TranslationDictionary.getInstance().getTranslation("branches-0");
        if (value == 1) format = TranslationDictionary.getInstance().getTranslation("branches-1");
        format += "\n";
        returnString += String.format(format, value);

        value = map.exampleCount();
        format = TranslationDictionary.getInstance().getTranslation("examples");
        if (value == 0) format = TranslationDictionary.getInstance().getTranslation("examples-0");
        if (value == 1) format = TranslationDictionary.getInstance().getTranslation("examples-1");
        format += "\n";
        returnString += String.format(format, value);

        value = map.cycleCount();
        format = TranslationDictionary.getInstance().getTranslation("cycles");
        if (value == 0) format = TranslationDictionary.getInstance().getTranslation("cycles-0");
        if (value == 1) format = TranslationDictionary.getInstance().getTranslation("cycles-1");
        format += "\n";
        returnString += String.format(format, value);

        value = map.subnetCount();
        format = TranslationDictionary.getInstance().getTranslation("subnets");
        if (value == 0) format = TranslationDictionary.getInstance().getTranslation("subnets-0");
        if (value == 1) format = TranslationDictionary.getInstance().getTranslation("subnets-1");
        returnString += String.format(format, value);
        return returnString;
    }

    private boolean similarConcepts() {
        this.checkConceptMaps();
        Map<Integer, List> studentOutgoingRelationships = this.studentMap.outgoingRelationships();
        Map<Integer, List> teacherOutgoingRelationships = this.teacherMap.outgoingRelationships();

        if (studentOutgoingRelationships.keySet().size() != teacherOutgoingRelationships.keySet().size()) return false;
        int initialSize = studentOutgoingRelationships.keySet().size();
        studentOutgoingRelationships.keySet().retainAll(teacherOutgoingRelationships.keySet());
        if (initialSize == studentOutgoingRelationships.keySet().size()) return true;
        return false;
    }

    private void checkConceptMaps() {
        if (this.teacherMap == null) throw new UnsupportedOperationException(NO_TEACHER_MAP);
        if (this.studentMap == null) throw new UnsupportedOperationException(NO_STUDENT_MAP);
    }

    @Override
    public String toString() {
        if (this.teacherMap != null)
            return "Student and teacher concept map scoring and comparison mode.";
        return "Student concept map scoring mode.";
    }
}
