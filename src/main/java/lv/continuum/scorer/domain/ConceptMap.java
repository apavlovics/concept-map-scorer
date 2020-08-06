package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.Translations;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public class ConceptMap {

    private static final Translations translations = Translations.getInstance();

    private static final String INVALID_XML = translations.get("invalid-xml");
    private static final String MAP_NO_CONCEPTS = translations.get("map-no-concepts");
    private static final String MAP_DUPLICATE_CONCEPTS = translations.get("map-duplicate-concepts");
    private static final String MAP_NO_RELATIONSHIPS = translations.get("map-no-relationships");
    private static final String MAP_INVALID_RELATIONSHIP = translations.get("map-invalid-relationship");

    private final List<Concept> concepts = new ArrayList<>();
    private final List<Relationship> relationships = new ArrayList<>();

    public ConceptMap(String xml) throws Exception {
        var documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        var file = new File(xml);
        var document = documentBuilder.parse(file);
        document.getDocumentElement().normalize();

        if (document.getDocumentElement().getNodeName().equals("conceptmap")) {
            System.out.println("Started parsing standard XML file");

            var conceptNodes = document.getElementsByTagName("concept");
            if (conceptNodes.getLength() == 0) {
                throw new IllegalArgumentException(String.format(MAP_NO_CONCEPTS, file.getName()));
            }
            for (int i = 0; i < conceptNodes.getLength(); i++) {
                var node = conceptNodes.item(i);
                if (containsConcept(node.getTextContent())) {
                    throw new IllegalArgumentException(String.format(MAP_DUPLICATE_CONCEPTS, file.getName()));
                }
                concepts.add(new Concept(node.getTextContent()));
            }

            var relationshipNodes = document.getElementsByTagName("relationship");
            if (relationshipNodes.getLength() == 0) {
                throw new IllegalArgumentException(String.format(MAP_NO_RELATIONSHIPS, file.getName()));
            }
            for (int i = 0; i < relationshipNodes.getLength(); i++) {
                var node = relationshipNodes.item(i);
                var from = Integer.parseInt(node.getAttributes().getNamedItem("from").getNodeValue());
                var to = Integer.parseInt(node.getAttributes().getNamedItem("to").getNodeValue());
                var fromConcept = concepts.get(from).id;
                var toConcept = concepts.get(to).id;
                relationships.add(new Relationship(fromConcept, toConcept, node.getTextContent()));
            }

            System.out.println("Finished parsing standard XML file\n" + toString());
        } else if (document.getDocumentElement().getAttributes().getNamedItem("name").getNodeValue().equals("root")) {
            System.out.println("Started parsing IKAS XML file");

            var elementNodes = document.getElementsByTagName("element");
            for (int i = 0; i < elementNodes.getLength(); i++) {
                var node = elementNodes.item(i);
                if (node.getAttributes().getNamedItem("name").getNodeValue().equals("node")) {
                    var nodeValue = node.getAttributes().getNamedItem("value").getNodeValue();
                    if (containsConcept(nodeValue)) {
                        throw new IllegalArgumentException(String.format(MAP_DUPLICATE_CONCEPTS, file.getName()));
                    }
                    concepts.add(new Concept(nodeValue));
                }
            }
            if (conceptCount() == 0) {
                throw new IllegalArgumentException(String.format(MAP_NO_CONCEPTS, file.getName()));
            }

            for (int i = 0; i < elementNodes.getLength(); i++) {
                var node = elementNodes.item(i);
                if (node.getAttributes().getNamedItem("name").getNodeValue().equals("relation")) {
                    String from = null;
                    String to = null;
                    var nodeValue = node.getAttributes().getNamedItem("value").getNodeValue();
                    var relationshipNodes = ((Element) node).getElementsByTagName("element");
                    for (int j = 0; j < relationshipNodes.getLength(); j++) {
                        var relationshipNode = relationshipNodes.item(j);
                        var relationshipNodeName = relationshipNode.getAttributes().getNamedItem("name").getNodeValue();
                        var relationshipNodeValue = relationshipNode.getAttributes().getNamedItem("value").getNodeValue();
                        if (relationshipNodeName.equals("source")) {
                            from = relationshipNodeValue;
                        } else if (relationshipNodeName.equals("target")) {
                            to = relationshipNodeValue;
                        }
                    }
                    if (from == null || to == null) {
                        throw new IllegalArgumentException(String.format(MAP_INVALID_RELATIONSHIP, file.getName()));
                    }

                    var fromConcept = -1;
                    var toConcept = -1;
                    for (Concept c : concepts) {
                        if (c.name.equals(from)) fromConcept = c.id;
                        if (c.name.equals(to)) toConcept = c.id;
                    }
                    relationships.add(new Relationship(fromConcept, toConcept, nodeValue));
                }
            }
            if (relationshipCount() == 0) {
                throw new IllegalArgumentException(String.format(MAP_NO_RELATIONSHIPS, file.getName()));
            }

            System.out.println("Finished parsing IKAS XML file\n" + toString());
        } else throw new IllegalArgumentException(String.format(INVALID_XML, file.getName()));
    }

    public int conceptCount() {
        return concepts.size();
    }

    public int relationshipCount() {
        return relationships.size();
    }

    // TODO Rework in functional style
    public int levelCount() {
        var levelCount = 0;
        var incomingRelationships = incomingRelationships();
        var currentConcepts = new HashSet<Integer>();
        if (incomingRelationships.containsValue(Set.of())) {
            while (currentConcepts.size() < conceptCount()) {
                var previousConceptsSize = currentConcepts.size();
                currentConcepts.clear();
                for (var ir : incomingRelationships.entrySet()) {
                    if (ir.getValue().isEmpty()) currentConcepts.add(ir.getKey());
                }
                if (currentConcepts.size() == previousConceptsSize) {
                    return 0;
                }
                for (var ir : incomingRelationships.entrySet()) {
                    var currentRelationships = ir.getValue();
                    if (!currentRelationships.isEmpty()) {
                        for (Integer cc : currentConcepts) {
                            if (currentRelationships.contains(cc)) ir.setValue(Set.of());
                        }
                    }
                }
                levelCount++;
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
        int result = 0;
        boolean isCycle;
        List<Integer> currentConcepts = new ArrayList<>();
        List<Integer> subnetConcepts = new ArrayList<>();
        var outgoingRelationships = outgoingRelationships();
        var incomingRelationships = incomingRelationships();

        int currentId = this.getFirstConceptId();
        while (currentConcepts.size() < this.conceptCount()) {
            subnetConcepts.clear();
            while (currentId >= 0) {
                if (!subnetConcepts.contains(currentId)) subnetConcepts.add(currentId);
                var currentOutgoingRelationships = outgoingRelationships.get(currentId);
                if (!currentOutgoingRelationships.isEmpty())
                    for (Integer cor : currentOutgoingRelationships) {
                        if (!subnetConcepts.contains(cor)) subnetConcepts.add(cor);
                        else {
                            isCycle = false;
                            var currentIncomingRelationships = incomingRelationships.get(cor);
                            for (Integer cir : currentIncomingRelationships)
                                if (!currentConcepts.contains(cir) &&
                                        subnetConcepts.contains(cir) &&
                                        subnetConcepts.indexOf(cir) >= subnetConcepts.indexOf(cor))
                                    isCycle = true;
                            if (isCycle) result++;
                        }
                    }
                if (subnetConcepts.indexOf(currentId) < subnetConcepts.size() - 1)
                    currentId = subnetConcepts.get(subnetConcepts.indexOf(currentId) + 1);
                else currentId = -1;
            }
            for (Integer i : subnetConcepts)
                if (!currentConcepts.contains(i)) currentConcepts.add(i);
            for (Concept c : this.concepts)
                if (!currentConcepts.contains(c.id)) {
                    currentId = c.id;
                    break;
                }
        }
        return result;
    }

    public int subnetCount() {
        int result = 0;
        List<Integer> currentConcepts = new ArrayList<>();
        var allRelationships = this.allRelationships();

        int currentId = this.getFirstConceptId();
        while (currentConcepts.size() < this.conceptCount()) {
            while (currentId >= 0) {
                if (!currentConcepts.contains(currentId)) currentConcepts.add(currentId);
                var currentRelationships = allRelationships.get(currentId);
                if (!currentRelationships.isEmpty())
                    for (Integer cr : currentRelationships)
                        if (!currentConcepts.contains(cr)) currentConcepts.add(cr);
                if (currentConcepts.indexOf(currentId) < currentConcepts.size() - 1)
                    currentId = currentConcepts.get(currentConcepts.indexOf(currentId) + 1);
                else currentId = -1;
            }
            for (Concept c : this.concepts) {
                if (!currentConcepts.contains(c.id)) {
                    currentId = c.id;
                    break;
                }
            }
            result++;
        }
        return result;
    }

    public Map<Integer, Set<Integer>> outgoingRelationships() {
        var outgoingRelationships = new HashMap<Integer, Set<Integer>>();
        for (Concept c : concepts) {
            outgoingRelationships.put(c.id, new HashSet<>());
        }
        for (Relationship r : relationships) {
            outgoingRelationships.get(r.fromConcept).add(r.toConcept);
        }
        return outgoingRelationships;
    }

    public Map<Integer, Set<Integer>> incomingRelationships() {
        var incomingRelationships = new HashMap<Integer, Set<Integer>>();
        for (Concept c : concepts) {
            incomingRelationships.put(c.id, new HashSet<>());
        }
        for (Relationship r : relationships) {
            incomingRelationships.get(r.toConcept).add(r.fromConcept);
        }
        return incomingRelationships;
    }

    public Map<Integer, Set<Integer>> allRelationships() {
        var allRelationships = new HashMap<Integer, Set<Integer>>();
        for (Concept c : concepts) {
            allRelationships.put(c.id, new HashSet<>());
        }
        for (Relationship r : relationships) {
            allRelationships.get(r.fromConcept).add(r.toConcept);
            allRelationships.get(r.toConcept).add(r.fromConcept);
        }
        return allRelationships;
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

    private int getFirstConceptId() {
        return concepts.get(0).id;
    }

    public boolean containsConcept(String name) {
        return concepts.stream()
                .anyMatch(c -> c.name.compareToIgnoreCase(name) == 0);
    }

    public boolean containsRelationship(int fromConcept, int toConcept) {
        return relationships.stream()
                .anyMatch(r -> r.fromConcept == fromConcept && r.toConcept == toConcept);
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
