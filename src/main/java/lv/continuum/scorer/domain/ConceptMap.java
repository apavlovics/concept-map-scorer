package lv.continuum.scorer.domain;

import lombok.extern.slf4j.Slf4j;
import lv.continuum.scorer.common.InvalidDataException;
import lv.continuum.scorer.common.Translations;
import org.apache.commons.collections4.set.ListOrderedSet;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

@Slf4j
public class ConceptMap {

    private static final Translations translations = Translations.getInstance();

    private static final Set<Concept> EMPTY = Set.of();

    private final Set<Concept> concepts;
    private final Set<Relationship> relationships;

    public final Map<Concept, Set<Concept>> outgoingRelationships;
    public final Map<Concept, Set<Concept>> incomingRelationships;
    public final Map<Concept, Set<Concept>> allRelationships;

    public ConceptMap(Set<Concept> concepts, Set<Relationship> relationships, String fileName) throws InvalidDataException {
        if (concepts.isEmpty()) {
            throw new InvalidDataException(String.format(translations.get("concept-map-no-concepts"), fileName));
        }
        if (relationships.isEmpty()) {
            throw new InvalidDataException(String.format(translations.get("concept-map-no-relationships"), fileName));
        }
        this.concepts = concepts;

        if (relationships.stream().anyMatch(r -> !concepts.contains(r.fromConcept) || !concepts.contains(r.toConcept))) {
            throw new InvalidDataException(String.format(translations.get("concept-map-invalid-relationship"), fileName));
        }
        this.relationships = relationships;

        outgoingRelationships = calculateRelationships(Set.of(Direction.OUTGOING));
        incomingRelationships = calculateRelationships(Set.of(Direction.INCOMING));
        allRelationships = calculateRelationships(Set.of(Direction.OUTGOING, Direction.INCOMING));
    }

    public long conceptCount() {
        return concepts.size();
    }

    public long relationshipCount() {
        return relationships.size();
    }

    public long levelCount() {
        var levelCount = 0L;
        var incomingRelationships = new HashMap<>(this.incomingRelationships);
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
        return relationships.stream().filter(r -> r.matches(regex)).count();
    }

    public boolean containsCycles() {
        var recursionStack = new HashSet<Concept>();
        var visited = new HashSet<Concept>();
        return outgoingRelationships.keySet().stream()
                .anyMatch(c -> subgraphContainsCycles(c, recursionStack, visited));
    }

    private boolean subgraphContainsCycles(Concept concept, Set<Concept> recursionStack, Set<Concept> visited) {
        if (recursionStack.contains(concept)) {
            return true;
        } else if (visited.contains(concept)) {
            return false;
        } else {
            recursionStack.add(concept);
            visited.add(concept);
            var containsCycleInSubgraph = outgoingRelationships.get(concept).stream()
                    .anyMatch(cor -> subgraphContainsCycles(cor, recursionStack, visited));
            recursionStack.remove(concept);
            return containsCycleInSubgraph;
        }
    }

    public long subnetCount() {
        var subnetCount = 0L;
        var currentConcept = anyConcept();
        var currentConcepts = new ListOrderedSet<Concept>();
        while (currentConcepts.size() < conceptCount()) {
            while (currentConcept.isPresent()) {
                var concept = currentConcept.get();
                currentConcepts.add(concept);
                currentConcepts.addAll(allRelationships.get(concept));
                var index = currentConcepts.indexOf(concept);
                currentConcept = index < currentConcepts.size() - 1 ?
                        Optional.of(currentConcepts.get(index + 1)) :
                        Optional.empty();
            }
            currentConcept = anyConceptNotIn(currentConcepts, currentConcept);
            subnetCount++;
        }
        return subnetCount;
    }

    /**
     * For each relationship in the concept map calculates all paths that include it.
     */
    public Map<Relationship, Set<Relationship>> allPaths() {
        var allPaths = new HashMap<Relationship, Set<Relationship>>();
        for (var r : relationships) {
            var paths = new HashSet<Relationship>();
            paths.add(r);

            var currentOutgoingConcepts = new ListOrderedSet<Concept>();
            var currentIncomingConcepts = new ListOrderedSet<Concept>();
            currentOutgoingConcepts.add(r.toConcept);
            currentIncomingConcepts.add(r.fromConcept);

            var o = 0;
            while (o < currentOutgoingConcepts.size()) {
                var currentOutgoingRelationships = outgoingRelationships.get(currentOutgoingConcepts.get(o++));
                for (var cor : currentOutgoingRelationships) {
                    currentOutgoingConcepts.add(cor);
                    paths.add(new Relationship(r.fromConcept, cor));
                }
            }

            var i = 0;
            while (i < currentIncomingConcepts.size()) {
                var currentIncomingRelationships = incomingRelationships.get(currentIncomingConcepts.get(i++));
                for (var cir : currentIncomingRelationships) {
                    currentIncomingConcepts.add(cir);
                    for (var coc : currentOutgoingConcepts) {
                        paths.add(new Relationship(cir, coc));
                    }
                }
            }
            allPaths.put(r, paths);
        }
        return allPaths;
    }

    public Set<List<Concept>> longestPaths() {
        var longestPaths = new HashSet<List<Concept>>();
        if (!containsCycles()) {
            incomingRelationships.entrySet().stream()
                    .filter(ir -> ir.getValue().isEmpty())
                    .forEach(ir -> {
                        var currentConcepts = new ArrayList<Concept>();
                        currentConcepts.add(ir.getKey());
                        var i = 0;
                        while (i < currentConcepts.size()) {
                            var currentConcept = currentConcepts.get(i);
                            var currentOutgoingRelationships = outgoingRelationships.get(currentConcept);
                            var pathsToAdd = new HashSet<List<Concept>>();
                            var pathsToRemove = new HashSet<List<Concept>>();
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
                    });
        }
        log.debug("Longest paths {}", longestPaths);
        return longestPaths;
    }

    /**
     * Checks if the other {@link ConceptMap} is similar to this one.
     * <p>
     * Two concept maps are considered similar if they contain equal concepts.
     */
    public boolean isSimilar(ConceptMap other) {
        return concepts.size() == other.concepts.size() && concepts.containsAll(other.concepts);
    }

    public boolean containsRelationship(Concept fromConcept, Concept toConcept) {
        return relationships.contains(new Relationship(fromConcept, toConcept));
    }

    /**
     * Represents relationship direction.
     */
    private enum Direction {
        OUTGOING, INCOMING;
    }

    private Map<Concept, Set<Concept>> calculateRelationships(Set<Direction> directions) {
        var relationships = new HashMap<Concept, Set<Concept>>();
        for (var c : concepts) {
            relationships.put(c, new HashSet<>());
        }
        for (var r : this.relationships) {
            if (directions.contains(Direction.OUTGOING)) {
                relationships.get(r.fromConcept).add(r.toConcept);
            }
            if (directions.contains(Direction.INCOMING)) {
                relationships.get(r.toConcept).add(r.fromConcept);
            }
        }
        // Use unmodifiable map to prevent undesired changes to its contents
        return Map.copyOf(relationships);
    }

    private Optional<Concept> anyConcept() {
        return concepts.stream().findAny();
    }

    private Optional<Concept> anyConceptNotIn(Set<Concept> subset, Optional<Concept> or) {
        return concepts.stream()
                .filter(c -> !subset.contains(c))
                .findFirst()
                .or(() -> or);
    }

    @Override
    public String toString() {
        return "Concept map with " + conceptCount() + " concepts and " + relationshipCount() + " relationships:\n" +
                "  Concepts " + concepts + "\n" +
                "  Relationships " + relationships;
    }
}
