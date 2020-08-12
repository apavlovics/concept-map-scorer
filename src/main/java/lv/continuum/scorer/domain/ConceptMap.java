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

    public ConceptMap(Set<Concept> concepts, Set<Relationship> relationships, String fileName) throws InvalidDataException {
        if (concepts.isEmpty()) {
            throw new InvalidDataException(String.format(translations.get("concept-map-no-concepts"), fileName));
        }
        if (relationships.isEmpty()) {
            throw new InvalidDataException(String.format(translations.get("concept-map-no-relationships"), fileName));
        }
        this.concepts = concepts;
        this.relationships = relationships;
    }

    public long conceptCount() {
        return concepts.size();
    }

    public long relationshipCount() {
        return relationships.size();
    }

    public long levelCount() {
        var levelCount = 0L;
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

    public long cycleCount() {
        var cycleCount = 0L;
        var currentConcept = anyConcept();
        var processedConcepts = new HashSet<Concept>();
        var outgoingRelationships = outgoingRelationships();
        while (processedConcepts.size() < conceptCount()) {
            var subnetConcepts = new ListOrderedSet<Concept>();
            while (currentConcept.isPresent()) {
                var concept = currentConcept.get();
                subnetConcepts.add(concept);
                for (var cor : outgoingRelationships.get(concept)) {
                    if (!subnetConcepts.add(cor)) {
                        cycleCount += 1;
                        log.debug("Cycle count increased to {}\n  Processed concepts {}\n  Subnet concepts {}\n  Relationship {}",
                                cycleCount, processedConcepts, subnetConcepts, new Relationship(concept, cor));
                    }
                }
                var index = subnetConcepts.indexOf(concept);
                currentConcept = index < subnetConcepts.size() - 1 ?
                        Optional.of(subnetConcepts.get(index + 1)) :
                        Optional.empty();
            }
            processedConcepts.addAll(subnetConcepts);
            currentConcept = anyConceptNotIn(processedConcepts, currentConcept);
        }
        return cycleCount;
    }

    public long subnetCount() {
        var subnetCount = 0L;
        var currentConcept = anyConcept();
        var currentConcepts = new ListOrderedSet<Concept>();
        var allRelationships = allRelationships();
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

    public Map<Relationship, Set<Relationship>> allPaths() {
        var allPaths = new HashMap<Relationship, Set<Relationship>>();
        var outgoingRelationships = outgoingRelationships();
        var incomingRelationships = incomingRelationships();
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
        if (cycleCount() == 0) {
            var outgoingRelationships = outgoingRelationships();
            var incomingRelationships = incomingRelationships();
            for (var ir : incomingRelationships.entrySet()) {
                if (ir.getValue().isEmpty()) {
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
                }
            }
        }
        return longestPaths;
    }

    /**
     * Checks if the other {@link ConceptMap} is similar to this one.
     * <p>
     * Two concept maps are considered similar if they contain equal concepts.
     */
    public boolean isSimilar(ConceptMap other) {
        return concepts.containsAll(other.concepts);
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

    public Map<Concept, Set<Concept>> outgoingRelationships() {
        return relationships(Set.of(Direction.OUTGOING));
    }

    public Map<Concept, Set<Concept>> incomingRelationships() {
        return relationships(Set.of(Direction.INCOMING));
    }

    public Map<Concept, Set<Concept>> allRelationships() {
        return relationships(Set.of(Direction.OUTGOING, Direction.INCOMING));
    }

    private Map<Concept, Set<Concept>> relationships(Set<Direction> directions) {
        var allRelationships = new HashMap<Concept, Set<Concept>>();
        for (var c : concepts) {
            allRelationships.put(c, new HashSet<>());
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
