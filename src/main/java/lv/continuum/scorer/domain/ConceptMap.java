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
import lv.continuum.scorer.common.TranslationDictionary;

/**
 * @author Andrey Pavlovich
 */
public class ConceptMap {
    private List<Concept> concepts;
    private List<Relationship> relationships;

    final public static String INVALID_XML = TranslationDictionary.getInstance().getTranslation("invalid-xml");
    final public static String MAP_NO_CONCEPTS = TranslationDictionary.getInstance().getTranslation("map-no-concepts");
    final public static String MAP_DUPLICATE_CONCEPTS = TranslationDictionary.getInstance().getTranslation("map-duplicate-concepts");
    final public static String MAP_NO_RELATIONSHIPS = TranslationDictionary.getInstance().getTranslation("map-no-relationships");
    final public static String MAP_INVALID_RELATIONSHIP = TranslationDictionary.getInstance().getTranslation("map-invalid-relationship");

    public ConceptMap(String xml) throws Exception {
            File file = new File(xml);
            NodeList nodeList;
            Node node;

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            if (doc.getDocumentElement().getNodeName().equals("conceptmap")) {
                System.out.println("Started parsing standard xml file.");
                nodeList = doc.getElementsByTagName("concept");
                if (nodeList.getLength() == 0)
                    throw new UnsupportedOperationException(String.format(MAP_NO_CONCEPTS, file.getName()));
                this.concepts = new ArrayList();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    node = nodeList.item(i);
                    if (this.containsConcept(node.getTextContent()))
                        throw new UnsupportedOperationException(String.format(MAP_DUPLICATE_CONCEPTS, file.getName()));
                    this.concepts.add(new Concept(node.getTextContent()));
                }

                nodeList = doc.getElementsByTagName("relationship");
                if (nodeList.getLength() == 0)
                    throw new UnsupportedOperationException(String.format(MAP_NO_RELATIONSHIPS, file.getName()));
                int from, fromConcept;
                int to, toConcept;
                this.relationships = new ArrayList();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    node = nodeList.item(i);
                    from = Integer.parseInt(node.getAttributes().getNamedItem("from").getNodeValue());
                    to   = Integer.parseInt(node.getAttributes().getNamedItem("to").getNodeValue());
                    fromConcept = this.concepts.get(from).getId();
                    toConcept   = this.concepts.get(to).getId();
                    this.relationships.add(new Relationship(fromConcept, toConcept, node.getTextContent()));
                }
                System.out.println("Finished parsing standard xml file.");
            } else if(doc.getDocumentElement().getAttributes().getNamedItem("name").getNodeValue().equals("root")) {
                System.out.println("Started parsing ikas xml file.");
                nodeList = doc.getElementsByTagName("element");
                this.concepts = new ArrayList();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    node = nodeList.item(i);
                    if (node.getAttributes().getNamedItem("name").getNodeValue().equals("node")) {
                        if (this.containsConcept(node.getAttributes().getNamedItem("value").getNodeValue()))
                            throw new UnsupportedOperationException(String.format(MAP_DUPLICATE_CONCEPTS, file.getName()));
                        this.concepts.add(new Concept(node.getAttributes().getNamedItem("value").getNodeValue()));
                    }
                }
                if (this.conceptCount() == 0)
                    throw new UnsupportedOperationException(String.format(MAP_NO_CONCEPTS, file.getName()));

                Node elementNode;
                NodeList elementNodeList;
                String from, to, name;
                int fromConcept, toConcept;
                this.relationships = new ArrayList();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    node = nodeList.item(i);
                    if (node.getAttributes().getNamedItem("name").getNodeValue().equals("relation")) {
                        from = null;
                        to = null;
                        fromConcept = -1;
                        toConcept = -1;

                        name = node.getAttributes().getNamedItem("value").getNodeValue();
                        elementNodeList = ((Element) node).getElementsByTagName("element");
                        for (int j = 0; j < elementNodeList.getLength(); j++) {
                            elementNode = elementNodeList.item(j);
                            if (elementNode.getAttributes().getNamedItem("name").getNodeValue().equals("source"))
                                from = elementNode.getAttributes().getNamedItem("value").getNodeValue();
                            if (elementNode.getAttributes().getNamedItem("name").getNodeValue().equals("target"))
                                to = elementNode.getAttributes().getNamedItem("value").getNodeValue();
                        }
                        if (from == null || to == null)
                            throw new UnsupportedOperationException(String.format(MAP_INVALID_RELATIONSHIP, file.getName()));

                        for (Concept c : this.concepts) {
                            if (c.getName().equals(from)) fromConcept = c.getId();
                            if (c.getName().equals(to)) toConcept = c.getId();
                        }
                        this.relationships.add(new Relationship(fromConcept, toConcept, name));
                    }
                }
                if (this.relationshipCount() == 0)
                    throw new UnsupportedOperationException(String.format(MAP_NO_RELATIONSHIPS, file.getName()));
                System.out.println("Finished parsing ikas xml file.");
            } else throw new UnsupportedOperationException(String.format(INVALID_XML, file.getName()));
    }

    final public int conceptCount() {
        return this.concepts.size();
    }

    final public int relationshipCount() {
        return this.relationships.size();
    }

    public int levelCount() {
        int result = 0;
        Map<Integer, List> incomingRelationships = this.incomingRelationships();
        if (incomingRelationships.containsValue(null)) {
            int sizeCheck = 0;
            List<Integer> currentConcepts = new ArrayList<Integer>();
            List<Integer> currentRelationships;
            while (currentConcepts.size() < this.conceptCount()) {
                sizeCheck = currentConcepts.size();
                currentConcepts.removeAll(currentConcepts);
                for (Map.Entry<Integer, List> ir : incomingRelationships.entrySet())
                    if (ir.getValue() == null) currentConcepts.add(ir.getKey());
                if (currentConcepts.size() == sizeCheck) return 0;
                for (Map.Entry<Integer, List> ir : incomingRelationships.entrySet()) {
                    if (ir.getValue() != null) {
                        currentRelationships = ir.getValue();
                        for (Integer cc : currentConcepts) if (currentRelationships.contains(cc)) ir.setValue(null);
                    }
                }
                result++;
            }
        }
        return result;
    }

    public int branchCount() {
        int result = 0, i = 0;
        Map<Integer, Integer> branches = new HashMap<Integer, Integer>();
        for (Concept c : this.concepts) branches.put(c.getId(), 0);
        for (Relationship r : this.relationships) {
            i = branches.get(r.getFromConcept());
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
        List<Integer> subnetConcepts  = new ArrayList<Integer>();
        List<Integer> currentOutgoingRelationships;
        List<Integer> currentIncomingRelationships;
        Map<Integer, List> outgoingRelationships = this.outgoingRelationships();
        Map<Integer, List> incomingRelationships = this.incomingRelationships();

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
                                        subnetConcepts.indexOf(cir) >= subnetConcepts.indexOf(cor)) isCycle = true;
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

    public Map<Integer, List> outgoingRelationships() {
        Map<Integer, List> resultMap = new HashMap<Integer, List>();
        List<Integer> i;
        for (Concept c : this.concepts) resultMap.put(c.getId(), null);
        for (Relationship r : this.relationships) {
            i = resultMap.get(r.getFromConcept());
            if (i == null) i = new ArrayList<Integer>();
            if (!i.contains(r.getToConcept())) i.add(r.getToConcept());
            resultMap.put(r.getFromConcept(), i);
        }
        return resultMap;
    }

    public Map<Integer, List> incomingRelationships() {
        Map<Integer, List> resultMap = new HashMap<Integer, List>();
        List<Integer> i;
        for (Concept c : this.concepts) resultMap.put(c.getId(), null);
        for (Relationship r : this.relationships) {
            i = resultMap.get(r.getToConcept());
            if (i == null) i = new ArrayList<Integer>();
            if (!i.contains(r.getFromConcept())) i.add(r.getFromConcept());
            resultMap.put(r.getToConcept(), i);
        }
        return resultMap;
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

    public Map<String, List> allPaths() {
        Map<String, List> resultMap = new HashMap<String, List>();

        int currentConcept;
        String keyPath, valuePath;
        List<String> i;
        List<Integer> currentOutgoingConcepts = new ArrayList();
        List<Integer> currentIncomingConcepts = new ArrayList();
        Map<Integer, List> outgoingRelationships = this.outgoingRelationships();
        Map<Integer, List> incomingRelationships = this.incomingRelationships();

        for (Relationship r : this.relationships) {
            currentOutgoingConcepts.removeAll(currentOutgoingConcepts);
            currentIncomingConcepts.removeAll(currentIncomingConcepts);
            currentOutgoingConcepts.add(r.getToConcept());
            currentIncomingConcepts.add(r.getFromConcept());

            i = new ArrayList<String>();
            keyPath = Integer.toString(r.getFromConcept()) + " " + Integer.toString(r.getToConcept());
            i.add(keyPath);

            currentConcept = 0;
            while (currentConcept < currentOutgoingConcepts.size()) {
                List<Integer> currentOutgoingRelationships = outgoingRelationships.get(currentOutgoingConcepts.get(currentConcept++));
                if (currentOutgoingRelationships == null) currentOutgoingRelationships = new ArrayList<Integer>();
                for (Integer cor : currentOutgoingRelationships) {
                    if (!currentOutgoingConcepts.contains(cor)) currentOutgoingConcepts.add(cor);
                    valuePath = Integer.toString(r.getFromConcept()) + " " +  Integer.toString(cor);
                    if (!i.contains(valuePath)) i.add(valuePath);
                }
            }
            currentConcept = 0;
            while (currentConcept < currentIncomingConcepts.size()) {
                List<Integer> currentIncomingRelationships = incomingRelationships.get(currentIncomingConcepts.get(currentConcept++));
                if (currentIncomingRelationships == null) currentIncomingRelationships = new ArrayList<Integer>();
                for (Integer cir : currentIncomingRelationships) {
                    if (!currentIncomingConcepts.contains(cir)) currentIncomingConcepts.add(cir);
                    for (Integer coc : currentOutgoingConcepts) {
                        valuePath = Integer.toString(cir) + " " + Integer.toString(coc);
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
        Map<Integer, List> outgoingRelationships = this.outgoingRelationships();
        Map<Integer, List> incomingRelationships = this.incomingRelationships();

        int currentConcept;
        int currentConceptIndex;
        for (Map.Entry<Integer, List> ir : incomingRelationships.entrySet())
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
                            }
                            else {
                                toAddResultList.removeAll(toAddResultList);
                                for (ArrayList<Integer> rl : resultList)
                                    if (rl.indexOf(currentConcept) == rl.size() - 1) {
                                        if (!toRemoveResultList.contains(rl)) toRemoveResultList.add(rl);
                                        newPath = (ArrayList)rl.clone();
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
