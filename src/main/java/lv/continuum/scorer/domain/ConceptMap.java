package lv.continuum.scorer.domain;

import org.apache.commons.collections4.list.SetUniqueList;
import org.apache.commons.collections4.set.ListOrderedSet;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

public class ConceptMap {

    // TODO Consider replacing lists with sets
    private final List<Concept> concepts;
    private final List<Relationship> relationships;

    public ConceptMap(List<Concept> concepts, List<Relationship> relationships) {
        this.concepts = concepts;
        this.relationships = relationships;
    }

    public int conceptCount() {
        return concepts.size();
    }

    public int relationshipCount() {
        return relationships.size();
    }

    public int levelCount() {
        var levelCount = 0;
        var incomingRelationships = incomingRelationships();
        if (incomingRelationships.containsValue(Set.of())) {
            var explicitLevels = true;
            Set<Integer> conceptsNoIncoming = Set.of();
            while (explicitLevels && conceptsNoIncoming.size() < conceptCount()) {
                var newConceptsNoIncoming = incomingRelationships.entrySet().stream()
                        .filter(e -> e.getValue().isEmpty())
                        .map(Map.Entry::getKey)
                        .collect(toSet());
                if (conceptsNoIncoming.size() == newConceptsNoIncoming.size()) {
                    explicitLevels = false;
                    levelCount = 0;
                } else {
                    conceptsNoIncoming = newConceptsNoIncoming;
                    for (var ir : incomingRelationships.entrySet()) {
                        for (var cni : conceptsNoIncoming) {
                            if (ir.getValue().contains(cni)) ir.setValue(Set.of());
                        }
                    }
                    levelCount++;
                }
            }
        }
        return levelCount;
    }

    public long branchCount() {
        return relationships.stream()
                .map(r -> r.fromConcept)
                .collect(groupingBy(Function.identity(), counting()))
                .values().stream()
                .filter(v -> v > 1)
                .count();
    }

    public long exampleCount() {
        var regex = "(?i).*(piemēr|piemer|eksemplār|eksemplar|example|instance).*";
        return relationships.stream().filter(r -> r.name.matches(regex)).count();
    }

    public int cycleCount() {
        var cycleCount = 0;
        var currentId = getFirstConceptId();
        var currentConcepts = new HashSet<Integer>();
        var outgoingRelationships = outgoingRelationships();
        var incomingRelationships = incomingRelationships();
        while (currentConcepts.size() < conceptCount()) {
            var subnetConcepts = new ListOrderedSet<Integer>();
            while (currentId >= 0) {
                subnetConcepts.add(currentId);
                var currentOutgoingRelationships = outgoingRelationships.get(currentId);
                for (var cor : currentOutgoingRelationships) {
                    if (!subnetConcepts.add(cor)) {
                        var currentIncomingRelationships = incomingRelationships.get(cor);
                        for (var cir : currentIncomingRelationships) {
                            if (!currentConcepts.contains(cir)
                                    && subnetConcepts.indexOf(cir) >= subnetConcepts.indexOf(cor)) {
                                cycleCount++;
                            }
                        }
                    }
                }
                var index = subnetConcepts.indexOf(currentId);
                currentId = index < subnetConcepts.size() - 1 ? subnetConcepts.get(index + 1) : -1;
            }
            currentConcepts.addAll(subnetConcepts);
            currentId = getFirstConceptIdNotIn(currentConcepts, currentId);
        }
        return cycleCount;
    }

    public int subnetCount() {
        var subnetCount = 0;
        var currentId = getFirstConceptId();
        var currentConcepts = new ListOrderedSet<Integer>();
        var allRelationships = allRelationships();
        while (currentConcepts.size() < conceptCount()) {
            while (currentId >= 0) {
                currentConcepts.add(currentId);
                currentConcepts.addAll(allRelationships.get(currentId));
                var index = currentConcepts.indexOf(currentId);
                currentId = index < currentConcepts.size() - 1 ? currentConcepts.get(index + 1) : -1;
            }
            currentId = getFirstConceptIdNotIn(currentConcepts, currentId);
            subnetCount++;
        }
        return subnetCount;
    }

    public Map<String, List<String>> allPaths() {
        var resultMap = new HashMap<String, List<String>>();

        int currentConcept;
        String keyPath, valuePath;
        List<String> i;
        List<Integer> currentOutgoingConcepts = new ArrayList<>();
        List<Integer> currentIncomingConcepts = new ArrayList<>();
        var outgoingRelationships = this.outgoingRelationships();
        var incomingRelationships = this.incomingRelationships();

        for (Relationship r : this.relationships) {
            currentOutgoingConcepts.clear();
            currentIncomingConcepts.clear();
            currentOutgoingConcepts.add(r.toConcept);
            currentIncomingConcepts.add(r.fromConcept);

            i = new ArrayList<>();
            keyPath = r.fromConcept + " " + r.toConcept;
            i.add(keyPath);

            currentConcept = 0;
            while (currentConcept < currentOutgoingConcepts.size()) {
                var currentOutgoingRelationships = outgoingRelationships.get(currentOutgoingConcepts.get(currentConcept++));
                for (Integer cor : currentOutgoingRelationships) {
                    if (!currentOutgoingConcepts.contains(cor)) currentOutgoingConcepts.add(cor);
                    valuePath = r.fromConcept + " " + cor;
                    if (!i.contains(valuePath)) i.add(valuePath);
                }
            }
            currentConcept = 0;
            while (currentConcept < currentIncomingConcepts.size()) {
                var currentIncomingRelationships = incomingRelationships.get(currentIncomingConcepts.get(currentConcept++));
                for (Integer cir : currentIncomingRelationships) {
                    if (!currentIncomingConcepts.contains(cir)) currentIncomingConcepts.add(cir);
                    for (Integer coc : currentOutgoingConcepts) {
                        valuePath = cir + " " + coc;
                        if (!i.contains(valuePath)) i.add(valuePath);
                    }
                }
            }
            resultMap.put(keyPath, i);
        }
        return resultMap;
    }

    public List<ArrayList> longestPaths() {
        if (this.cycleCount() > 0) return null;
        List<ArrayList> resultList = new ArrayList<ArrayList>();
        List<ArrayList> toAddResultList = new ArrayList<ArrayList>();
        List<ArrayList> toRemoveResultList = new ArrayList<ArrayList>();
        ArrayList<Integer> path;
        ArrayList<Integer> newPath;

        List<Integer> currentConcepts = new ArrayList<Integer>();
        var outgoingRelationships = outgoingRelationships();
        var incomingRelationships = incomingRelationships();

        int currentConcept;
        int currentConceptIndex;
        for (var ir : incomingRelationships.entrySet())
            if (ir.getValue().isEmpty()) {
                currentConcepts.clear();
                currentConcepts.add(ir.getKey());
                currentConceptIndex = 0;
                while (currentConceptIndex < currentConcepts.size()) {
                    currentConcept = currentConcepts.get(currentConceptIndex);
                    var currentOutgoingRelationships = outgoingRelationships.get(currentConcept);
                    if (!currentOutgoingRelationships.isEmpty()) {
                        toRemoveResultList.clear();
                        for (Integer cor : currentOutgoingRelationships) {
                            currentConcepts.add(cor);
                            if (currentConceptIndex == 0) {
                                path = new ArrayList<>();
                                path.add(currentConcept);
                                path.add(cor);
                                resultList.add(path);
                            } else {
                                toAddResultList.removeAll(toAddResultList);
                                for (ArrayList<Integer> rl : resultList)
                                    if (rl.indexOf(currentConcept) == rl.size() - 1) {
                                        if (!toRemoveResultList.contains(rl)) toRemoveResultList.add(rl);
                                        newPath = (ArrayList) rl.clone();
                                        newPath.add(cor);
                                        toAddResultList.add(newPath);
                                    }
                                resultList.addAll(toAddResultList);
                            }
                        }
                        resultList.removeAll(toRemoveResultList);
                    }
                    currentConceptIndex++;
                }
            }
        System.out.println("Longest paths: " + resultList);
        return resultList;
    }

    public boolean containsRelationship(int fromConcept, int toConcept) {
        return relationships.stream()
                .anyMatch(r -> r.fromConcept == fromConcept && r.toConcept == toConcept);
    }

    /**
     * Represents relationship direction.
     */
    private enum Direction {
        OUTGOING, INCOMING;
    }

    public Map<Integer, Set<Integer>> outgoingRelationships() {
        return relationships(Set.of(Direction.OUTGOING));
    }

    public Map<Integer, Set<Integer>> incomingRelationships() {
        return relationships(Set.of(Direction.INCOMING));
    }

    public Map<Integer, Set<Integer>> allRelationships() {
        return relationships(Set.of(Direction.OUTGOING, Direction.INCOMING));
    }

    private Map<Integer, Set<Integer>> relationships(Set<Direction> directions) {
        var allRelationships = new HashMap<Integer, Set<Integer>>();
        for (Concept c : concepts) {
            allRelationships.put(c.id, new HashSet<>());
        }
        for (Relationship r : relationships) {
            if (directions.contains(Direction.OUTGOING)) {
                allRelationships.get(r.fromConcept).add(r.toConcept);
            }
            if (directions.contains(Direction.INCOMING)) {
                allRelationships.get(r.toConcept).add(r.fromConcept);
            }
        }
        return allRelationships;
    }

    private int getFirstConceptId() {
        return concepts.get(0).id;
    }

    private int getFirstConceptIdNotIn(Set<Integer> conceptIds, int defaultId) {
        return concepts.stream()
                .filter(c -> !conceptIds.contains(c.id))
                .findFirst()
                .map(c -> c.id)
                .orElse(defaultId);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("Concept map with ")
                .append(conceptCount())
                .append(conceptCount() == 1 ? " concept and " : " concepts and ")
                .append(relationshipCount())
                .append(relationshipCount() == 1 ? " relationship.\n" : " relationships\n");
        for (Concept c : concepts) sb.append(c).append("\n");
        for (Relationship r : relationships) sb.append(r).append("\n");
        return sb.substring(0, sb.length() - 1);
    }
}
