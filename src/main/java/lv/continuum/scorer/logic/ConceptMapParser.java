package lv.continuum.scorer.logic;

import lv.continuum.scorer.common.InvalidDataException;
import lv.continuum.scorer.common.Translations;
import lv.continuum.scorer.domain.Concept;
import lv.continuum.scorer.domain.ConceptMap;
import lv.continuum.scorer.domain.Relationship;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class ConceptMapParser {

    private static final Translations translations = Translations.getInstance();

    private static final String INVALID_XML = translations.get("invalid-xml");
    private static final String MAP_DUPLICATE_CONCEPTS = translations.get("map-duplicate-concepts");
    private static final String MAP_INVALID_RELATIONSHIP = translations.get("map-invalid-relationship");

    public ConceptMap parse(String xml) throws IOException, ParserConfigurationException, SAXException, InvalidDataException {
        var documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        var file = new File(xml);
        var document = documentBuilder.parse(file);
        document.getDocumentElement().normalize();

        try {
            var fileName = file.getName();
            if (document.getDocumentElement().getNodeName().equals("conceptmap")) {
                return parseStandard(document, fileName);
            } else if (document.getDocumentElement().getAttributes().getNamedItem("name").getNodeValue().equals("root")) {
                return parseIkas(document, fileName);
            } else throw new InvalidDataException(String.format(INVALID_XML, file.getName()));
        } catch (NumberFormatException | NullPointerException e) {
            throw new InvalidDataException(String.format(INVALID_XML, file.getName()));
        }
    }

    private ConceptMap parseStandard(Document document, String fileName) throws InvalidDataException {
        System.out.println("Started parsing standard XML file");

        var conceptNodes = document.getElementsByTagName("concept");
        var concepts = new HashMap<Integer, Concept>();
        for (var i = 0; i < conceptNodes.getLength(); i++) {
            var node = conceptNodes.item(i);
            var name = node.getTextContent();
            if (hasConceptWithDuplicateName(concepts.values(), name)) {
                throw new InvalidDataException(String.format(MAP_DUPLICATE_CONCEPTS, fileName));
            }
            var id = Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue());
            concepts.put(id, new Concept(id, name));
        }

        var relationshipNodes = document.getElementsByTagName("relationship");
        var relationships = new HashSet<Relationship>();
        for (var i = 0; i < relationshipNodes.getLength(); i++) {
            var node = relationshipNodes.item(i);
            var from = Integer.parseInt(node.getAttributes().getNamedItem("from").getNodeValue());
            var to = Integer.parseInt(node.getAttributes().getNamedItem("to").getNodeValue());
            var fromConcept = concepts.get(from).id;
            var toConcept = concepts.get(to).id;
            relationships.add(new Relationship(fromConcept, toConcept, node.getTextContent()));
        }

        var conceptMap = new ConceptMap(new HashSet<>(concepts.values()), relationships, fileName);
        System.out.println("Finished parsing standard XML file");
        System.out.println(conceptMap);
        return conceptMap;
    }

    private ConceptMap parseIkas(Document document, String fileName) throws InvalidDataException {
        System.out.println("Started parsing IKAS XML file");

        var concepts = new HashSet<Concept>();
        var conceptLowerCaseNames = new ListOrderedSet<String>();
        var elementNodes = document.getElementsByTagName("element");
        for (var i = 0; i < elementNodes.getLength(); i++) {
            var node = elementNodes.item(i);
            if (node.getAttributes().getNamedItem("name").getNodeValue().equals("node")) {
                var name = node.getAttributes().getNamedItem("value").getNodeValue();
                if (hasConceptWithDuplicateName(concepts, name)) {
                    throw new InvalidDataException(String.format(MAP_DUPLICATE_CONCEPTS, fileName));
                }
                var lowerCaseName = name.toLowerCase();
                conceptLowerCaseNames.add(lowerCaseName);
                var id = conceptLowerCaseNames.indexOf(lowerCaseName);
                concepts.add(new Concept(id, name));
            }
        }

        var relationships = new HashSet<Relationship>();
        for (var i = 0; i < elementNodes.getLength(); i++) {
            var node = elementNodes.item(i);
            if (node.getAttributes().getNamedItem("name").getNodeValue().equals("relation")) {
                String from = null;
                String to = null;
                var nodeValue = node.getAttributes().getNamedItem("value").getNodeValue();
                var relationshipNodes = ((Element) node).getElementsByTagName("element");
                for (var j = 0; j < relationshipNodes.getLength(); j++) {
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
                    throw new InvalidDataException(String.format(MAP_INVALID_RELATIONSHIP, fileName));
                }

                var fromConcept = -1;
                var toConcept = -1;
                for (var c : concepts) {
                    if (c.name.equals(from)) fromConcept = c.id;
                    if (c.name.equals(to)) toConcept = c.id;
                }
                relationships.add(new Relationship(fromConcept, toConcept, nodeValue));
            }
        }

        var conceptMap = new ConceptMap(concepts, relationships, fileName);
        System.out.println("Finished parsing IKAS XML file");
        System.out.println(conceptMap);
        return conceptMap;
    }

    private boolean hasConceptWithDuplicateName(Collection<Concept> concepts, String name) {
        return concepts.stream().anyMatch(c -> c.hasDuplicateName(name));
    }
}
