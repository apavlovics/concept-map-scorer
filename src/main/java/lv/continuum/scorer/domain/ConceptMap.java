package lv.continuum.scorer.domain;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import lv.continuum.scorer.common.Translations;

/**
 * @author Andrey Pavlovich
 */
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
        var builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        var file = new File(xml);
        var doc = builder.parse(file);
        doc.getDocumentElement().normalize();

        if (doc.getDocumentElement().getNodeName().equals("conceptmap")) {
            System.out.println("Started parsing standard XML file.");

            var concepts = doc.getElementsByTagName("concept");
            if (concepts.getLength() == 0) {
                throw new UnsupportedOperationException(String.format(MAP_NO_CONCEPTS, file.getName()));
            }
            for (int i = 0; i < concepts.getLength(); i++) {
                var item = concepts.item(i);
                if (this.containsConcept(item.getTextContent())) {
                    throw new UnsupportedOperationException(String.format(MAP_DUPLICATE_CONCEPTS, file.getName()));
                }
                this.concepts.add(new Concept(item.getTextContent()));
            }

            var relationships = doc.getElementsByTagName("relationship");
            if (relationships.getLength() == 0) {
                throw new UnsupportedOperationException(String.format(MAP_NO_RELATIONSHIPS, file.getName()));
            }
            for (int i = 0; i < relationships.getLength(); i++) {
                var item = relationships.item(i);
                var from = Integer.parseInt(item.getAttributes().getNamedItem("from").getNodeValue());
                var to = Integer.parseInt(item.getAttributes().getNamedItem("to").getNodeValue());
                var fromConcept = this.concepts.get(from).getId();
                var toConcept = this.concepts.get(to).getId();
                this.relationships.add(new Relationship(fromConcept, toConcept, item.getTextContent()));
            }

            System.out.println("Finished parsing standard XML file.");
        } else if (doc.getDocumentElement().getAttributes().getNamedItem("name").getNodeValue().equals("root")) {
            System.out.println("Started parsing IKAS XML file.");

            var elements = doc.getElementsByTagName("element");
            for (int i = 0; i < elements.getLength(); i++) {
                var item = elements.item(i);
                if (item.getAttributes().getNamedItem("name").getNodeValue().equals("node")) {
                    if (containsConcept(item.getAttributes().getNamedItem("value").getNodeValue())) {
                        throw new UnsupportedOperationException(String.format(MAP_DUPLICATE_CONCEPTS, file.getName()));
                    }
                    this.concepts.add(new Concept(item.getAttributes().getNamedItem("value").getNodeValue()));
                }
            }
            if (this.conceptCount() == 0) {
                throw new UnsupportedOperationException(String.format(MAP_NO_CONCEPTS, file.getName()));
            }

            for (int i = 0; i < elements.getLength(); i++) {
                var item = elements.item(i);
                if (item.getAttributes().getNamedItem("name").getNodeValue().equals("relation")) {
                    String from = null;
                    String to = null;
                    var name = item.getAttributes().getNamedItem("value").getNodeValue();
                    var itemRelationships = ((Element) item).getElementsByTagName("element");
                    for (int j = 0; j < itemRelationships.getLength(); j++) {
                        var itemRelationship = itemRelationships.item(j);
                        var itemRelationshipName = itemRelationship.getAttributes().getNamedItem("name").getNodeValue();
                        var itemRelationshipValue = itemRelationship.getAttributes().getNamedItem("value").getNodeValue();
                        if (itemRelationshipName.equals("source")) {
                            from = itemRelationshipValue;
                        } else if (itemRelationshipName.equals("target")) {
                            to = itemRelationshipValue;
                        }
                    }
                    if (from == null || to == null) {
                        throw new UnsupportedOperationException(String.format(MAP_INVALID_RELATIONSHIP, file.getName()));
                    }

                    var fromConcept = -1;
                    var toConcept = -1;
                    for (Concept c : this.concepts) {
                        if (c.getName().equals(from)) fromConcept = c.getId();
                        if (c.getName().equals(to)) toConcept = c.getId();
                    }
                    this.relationships.add(new Relationship(fromConcept, toConcept, name));
                }
            }
            if (this.relationshipCount() == 0) {
                throw new UnsupportedOperationException(String.format(MAP_NO_RELATIONSHIPS, file.getName()));
            }

            System.out.println("Finished parsing IKAS XML file.");
        } else throw new UnsupportedOperationException(String.format(INVALID_XML, file.getName()));
    }

    public int conceptCount() {
        return this.concepts.size();
    }

    public int relationshipCount() {
        return this.relationships.size();
    }

    public int levelCount() {
        var levelCount = 0;
        var incomingRelationships = incomingRelationships();
        if (incomingRelationships.containsValue(null)) {
            List<Integer> currentConcepts = new ArrayList<>();
            while (currentConcepts.size() < this.conceptCount()) {
                int sizeCheck = currentConcepts.size();
                currentConcepts.clear();
                for (var ir : incomingRelationships.entrySet()) {
                    if (ir.getValue() == null) currentConcepts.add(ir.getKey());
                }
                if (currentConcepts.size() == sizeCheck) {
                    return 0;
                }
                for (var ir : incomingRelationships.entrySet()) {
                    if (ir.getValue() != null) {
                        var currentRelationships = ir.getValue();
                        for (Integer cc : currentConcepts) {
                            if (currentRelationships.contains(cc)) ir.setValue(null);
                        }
                    }
                }
                levelCount++;
            }
        }
        return levelCount;
    }

    public int branchCount() {
        int result = 0;
        Map<Integer, Integer> branches = new HashMap<Integer, Integer>();
        for (Concept c : this.concepts) branches.put(c.getId(), 0);
        for (Relationship r : this.relationships) {
            var i = branches.get(r.getFromConcept());
            if (i == 1) result++;
            branches.put(r.getFromConcept(), ++i);
        }
        return result;
    }

    public int exampleCount() {
        int result = 0;
        String regex = "(?i).*(piemēr|piemer|eksemplār|eksemplar|example|instance).*";
        for (Relationship r : this.relationships)
            if (r.getName().matches(regex)) result++;
        return result;
    }

    public int cycleCount() {
        int result = 0;
        boolean isCycle;
        List<Integer> currentConcepts = new ArrayList<Integer>();
        List<Integer> subnetConcepts = new ArrayList<Integer>();
        List<Integer> currentOutgoingRelationships;
        List<Integer> currentIncomingRelationships;
        var outgoingRelationships = this.outgoingRelationships();
        var incomingRelationships = this.incomingRelationships();

        int currentId = this.getFirstConceptId();
        while (currentConcepts.size() < this.conceptCount()) {
            subnetConcepts.removeAll(subnetConcepts);
            while (currentId >= 0) {
                if (!subnetConcepts.contains(currentId)) subnetConcepts.add(currentId);
                currentOutgoingRelationships = outgoingRelationships.get(currentId);
                if (currentOutgoingRelationships != null)
                    for (Integer cor : currentOutgoingRelationships) {
                        if (!subnetConcepts.contains(cor)) subnetConcepts.add(cor);
                        else {
                            isCycle = false;
                            currentIncomingRelationships = incomingRelationships.get(cor);
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
                if (!currentConcepts.contains(c.getId())) {
                    currentId = c.getId();
                    break;
                }
        }
        return result;
    }

    public int subnetCount() {
        int result = 0;
        List<Integer> currentConcepts = new ArrayList<Integer>();
        List<Integer> currentRelationships;
        Map<Integer, List> allRelationships = this.allRelationships();

        int currentId = this.getFirstConceptId();
        while (currentConcepts.size() < this.conceptCount()) {
            while (currentId >= 0) {
                if (!currentConcepts.contains(currentId)) currentConcepts.add(currentId);
                currentRelationships = allRelationships.get(currentId);
                if (currentRelationships != null)
                    for (Integer cr : currentRelationships)
                        if (!currentConcepts.contains(cr)) currentConcepts.add(cr);
                if (currentConcepts.indexOf(currentId) < currentConcepts.size() - 1)
                    currentId = currentConcepts.get(currentConcepts.indexOf(currentId) + 1);
                else currentId = -1;
            }
            for (Concept c : this.concepts)
                if (!currentConcepts.contains(c.getId())) {
                    currentId = c.getId();
                    break;
                }
            result++;
        }
        return result;
    }

    public Map<Integer, List<Integer>> outgoingRelationships() {
        var relationships = new HashMap<Integer, List<Integer>>();
        for (Concept c : this.concepts) {
            relationships.put(c.getId(), null);
        }
        for (Relationship r : this.relationships) {
            var ids = relationships.get(r.getFromConcept());
            if (ids == null) ids = new ArrayList<>();
            if (!ids.contains(r.getToConcept())) ids.add(r.getToConcept());
            relationships.put(r.getFromConcept(), ids);
        }
        return relationships;
    }

    public Map<Integer, List<Integer>> incomingRelationships() {
        var relationships = new HashMap<Integer, List<Integer>>();
        for (Concept c : this.concepts) {
            relationships.put(c.getId(), null);
        }
        for (Relationship r : this.relationships) {
            var ids = relationships.get(r.getToConcept());
            if (ids == null) ids = new ArrayList<>();
            if (!ids.contains(r.getFromConcept())) ids.add(r.getFromConcept());
            relationships.put(r.getToConcept(), ids);
        }
        return relationships;
    }

    public Map<Integer, List> allRelationships() {
        Map<Integer, List> resultMap = new HashMap<Integer, List>();
        List<Integer> i;
        for (Concept c : this.concepts) resultMap.put(c.getId(), null);
        for (Relationship r : this.relationships) {
            i = resultMap.get(r.getFromConcept());
            if (i == null) i = new ArrayList<Integer>();
            if (!i.contains(r.getToConcept())) i.add(r.getToConcept());
            resultMap.put(r.getFromConcept(), i);

            i = resultMap.get(r.getToConcept());
            if (i == null) i = new ArrayList<Integer>();
            if (!i.contains(r.getFromConcept())) i.add(r.getFromConcept());
            resultMap.put(r.getToConcept(), i);
        }
        return resultMap;
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
            currentOutgoingConcepts.add(r.getToConcept());
            currentIncomingConcepts.add(r.getFromConcept());

            i = new ArrayList<>();
            keyPath = r.getFromConcept() + " " + r.getToConcept();
            i.add(keyPath);

            currentConcept = 0;
            while (currentConcept < currentOutgoingConcepts.size()) {
                List<Integer> currentOutgoingRelationships = outgoingRelationships.get(currentOutgoingConcepts.get(currentConcept++));
                if (currentOutgoingRelationships == null)
                    currentOutgoingRelationships = new ArrayList<>();
                for (Integer cor : currentOutgoingRelationships) {
                    if (!currentOutgoingConcepts.contains(cor)) currentOutgoingConcepts.add(cor);
                    valuePath = r.getFromConcept() + " " + cor;
                    if (!i.contains(valuePath)) i.add(valuePath);
                }
            }
            currentConcept = 0;
            while (currentConcept < currentIncomingConcepts.size()) {
                List<Integer> currentIncomingRelationships = incomingRelationships.get(currentIncomingConcepts.get(currentConcept++));
                if (currentIncomingRelationships == null)
                    currentIncomingRelationships = new ArrayList<>();
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
        List<Integer> currentOutgoingRelationships;
        var outgoingRelationships = this.outgoingRelationships();
        var incomingRelationships = this.incomingRelationships();

        int currentConcept;
        int currentConceptIndex;
        for (var ir : incomingRelationships.entrySet())
            if (ir.getValue() == null) {
                currentConcepts.removeAll(currentConcepts);
                currentConcepts.add(ir.getKey());
                currentConceptIndex = 0;
                while (currentConceptIndex < currentConcepts.size()) {
                    currentConcept = currentConcepts.get(currentConceptIndex);
                    currentOutgoingRelationships = outgoingRelationships.get(currentConcept);
                    if (currentOutgoingRelationships != null) {
                        toRemoveResultList.removeAll(toRemoveResultList);
                        for (Integer cor : currentOutgoingRelationships) {
                            currentConcepts.add(cor);
                            if (currentConceptIndex == 0) {
                                path = new ArrayList<Integer>();
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
        return this.concepts.get(0).getId();
    }

    private boolean containsConcept(String name) {
        for (Concept c : this.concepts)
            if (c.getName().compareToIgnoreCase(name) == 0) return true;
        return false;
    }

    public boolean containsRelationship(int fromConcept, int toConcept) {
        for (Relationship r : this.relationships)
            if (r.getFromConcept() == fromConcept && r.getToConcept() == toConcept) return true;
        return false;
    }

    @Override
    public String toString() {
        String returnString;
        String conceptLabel, relationshipLabel;
        if (this.conceptCount() == 1) conceptLabel = " concept and ";
        else conceptLabel = " concepts and ";
        if (this.relationshipCount() == 1) relationshipLabel = " relationship.";
        else relationshipLabel = " relationships.";

        returnString = "Concept map with " +
                this.conceptCount() + conceptLabel +
                this.relationshipCount() + relationshipLabel + "\n";
        for (Concept c : this.concepts) returnString += c.toString() + "\n";
        for (Relationship r : this.relationships) returnString += r.toString() + "\n";
        returnString = returnString.substring(0, returnString.length() - 1);
        return returnString;
    }
}
