package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.InvalidDataException;
import lv.continuum.scorer.common.Translations;
import org.apache.commons.collections4.set.ListOrderedSet;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

public class ConceptMap {

    private static final Translations translations = Translations.getInstance();

    private static final String MAP_NO_CONCEPTS = translations.get("map-no-concepts");
    private static final String MAP_NO_RELATIONSHIPS = translations.get("map-no-relationships");
    private static final Set<Integer> EMPTY = Set.of();

    private final Set<Concept> concepts;
    private final Set<Relationship> relationships;

    public ConceptMap(Set<Concept> concepts, Set<Relationship> relationships, String fileName) throws InvalidDataException {
        if (concepts.isEmpty()) {
            throw new InvalidDataException(String.format(MAP_NO_CONCEPTS, fileName));
        }
        if (relationships.isEmpty()) {
            throw new InvalidDataException(String.format(MAP_NO_RELATIONSHIPS, fileName));
        }
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
        if (incomingRelationships.containsValue(EMPTY)) {
            var explicitLevels = true;
            var conceptsNoIncoming = EMPTY;
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
                            if (ir.getValue().contains(cni)) ir.setValue(EMPTY);
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

    // TODO This algorithm contains mistake
    public int cycleCount() {
        var cycleCount = 0;
        var currentId = anyConceptId();
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
                                System.out.println("Subnet is " + subnetConcepts + ", will increse cycle count to " + cycleCount);
                                cycleCount++;
                            }
                        }
                    }
                }
                var index = subnetConcepts.indexOf(currentId);
                currentId = index < subnetConcepts.size() - 1 ? subnetConcepts.get(index + 1) : -1;
            }
            currentConcepts.addAll(subnetConcepts);
            currentId = findFirstConceptIdNotIn(currentConcepts, currentId);
        }
        return cycleCount;
    }

    public int subnetCount() {
        var subnetCount = 0;
        var currentId = anyConceptId();
        var currentConcepts = new ListOrderedSet<Integer>();
        var allRelationships = allRelationships();
        while (currentConcepts.size() < conceptCount()) {
            while (currentId >= 0) {
                currentConcepts.add(currentId);
                currentConcepts.addAll(allRelationships.get(currentId));
                var index = currentConcepts.indexOf(currentId);
                currentId = index < currentConcepts.size() - 1 ? currentConcepts.get(index + 1) : -1;
            }
            currentId = findFirstConceptIdNotIn(currentConcepts, currentId);
            subnetCount++;
        }
        return subnetCount;
    }

    public Map<String, Set<String>> allPaths() {
        var allPaths = new HashMap<String, Set<String>>();
        var outgoingRelationships = outgoingRelationships();
        var incomingRelationships = incomingRelationships();
        for (var r : relationships) {
            var valuePaths = new HashSet<String>();
            var keyPath = r.fromConcept + " " + r.toConcept;
            valuePaths.add(keyPath);

            var currentOutgoingConcepts = new ListOrderedSet<Integer>();
            var currentIncomingConcepts = new ListOrderedSet<Integer>();
            currentOutgoingConcepts.add(r.toConcept);
            currentIncomingConcepts.add(r.fromConcept);

            var o = 0;
            while (o < currentOutgoingConcepts.size()) {
                var currentOutgoingRelationships = outgoingRelationships.get(currentOutgoingConcepts.get(o++));
                for (var cor : currentOutgoingRelationships) {
                    currentOutgoingConcepts.add(cor);
                    valuePaths.add(r.fromConcept + " " + cor);
                }
            }

            var i = 0;
            while (i < currentIncomingConcepts.size()) {
                var currentIncomingRelationships = incomingRelationships.get(currentIncomingConcepts.get(i++));
                for (var cir : currentIncomingRelationships) {
                    currentIncomingConcepts.add(cir);
                    for (var coc : currentOutgoingConcepts) {
                        valuePaths.add(cir + " " + coc);
                    }
                }
            }
            allPaths.put(keyPath, valuePaths);
        }
        return allPaths;
    }

    public Set<List<Integer>> longestPaths() {
        var longestPaths = new HashSet<List<Integer>>();
        if (cycleCount() == 0) {
            var outgoingRelationships = outgoingRelationships();
            var incomingRelationships = incomingRelationships();
            for (var ir : incomingRelationships.entrySet()) {
                if (ir.getValue().isEmpty()) {
                    var currentConcepts = new ArrayList<Integer>();
                    currentConcepts.add(ir.getKey());
                    var i = 0;
                    while (i < currentConcepts.size()) {
                        var currentConcept = currentConcepts.get(i);
                        var currentOutgoingRelationships = outgoingRelationships.get(currentConcept);
                        var pathsToAdd = new HashSet<List<Integer>>();
                        var pathsToRemove = new HashSet<List<Integer>>();
                        for (var cor : currentOutgoingRelationships) {
                            currentConcepts.add(cor);
                            if (i == 0) {
                                var path = List.of(currentConcept, cor);
                                longestPaths.add(path);
                            } else for (var path : longestPaths) {
                                if (path.indexOf(currentConcept) == path.size() - 1) {
                                    var longerPath = new ArrayList<>(path);
                                    longerPath.add(cor);
                                    pathsToAdd.add(longerPath);
                                    pathsToRemove.add(path);
                                }
                            }
                        }
                        longestPaths.addAll(pathsToAdd);
                        longestPaths.removeAll(pathsToRemove);
                        i++;
                    }
                }
            }
        }
        return longestPaths;
    }

    public boolean isSimilar(ConceptMap other) {
        return concepts.size() == other.concepts.size() &&
                concepts.stream().allMatch(c -> other.concepts.stream().anyMatch(c::isSimilar));
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
        for (var c : concepts) {
            allRelationships.put(c.id, new HashSet<>());
        }
        for (var r : relationships) {
            if (directions.contains(Direction.OUTGOING)) {
                allRelationships.get(r.fromConcept).add(r.toConcept);
            }
            if (directions.contains(Direction.INCOMING)) {
                allRelationships.get(r.toConcept).add(r.fromConcept);
            }
        }
        return allRelationships;
    }

    private int anyConceptId() {
        return concepts.iterator().next().id;
    }

    private int findFirstConceptIdNotIn(Set<Integer> conceptIds, int defaultId) {
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
        for (var c : concepts) sb.append(c).append("\n");
        for (var r : relationships) sb.append(r).append("\n");
        return sb.substring(0, sb.length() - 1);
    }
}
