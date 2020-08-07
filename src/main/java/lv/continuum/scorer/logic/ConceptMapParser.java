package lv.continuum.scorer.logic;

import lv.continuum.scorer.common.InvalidDataException;
import lv.continuum.scorer.common.Translations;
import lv.continuum.scorer.domain.Concept;
import lv.continuum.scorer.domain.ConceptMap;
import lv.continuum.scorer.domain.Relationship;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConceptMapParser {

    private static final Translations translations = Translations.getInstance();

    private static final String INVALID_XML = translations.get("invalid-xml");
    private static final String MAP_NO_CONCEPTS = translations.get("map-no-concepts");
    private static final String MAP_DUPLICATE_CONCEPTS = translations.get("map-duplicate-concepts");
    private static final String MAP_NO_RELATIONSHIPS = translations.get("map-no-relationships");
    private static final String MAP_INVALID_RELATIONSHIP = translations.get("map-invalid-relationship");

    public ConceptMap parse(String xml) throws IOException, ParserConfigurationException, SAXException, InvalidDataException {
        var documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        var file = new File(xml);
        var document = documentBuilder.parse(file);
        document.getDocumentElement().normalize();

        var fileName = file.getName();
        if (document.getDocumentElement().getNodeName().equals("conceptmap")) {
            return parseStandard(document, fileName);
        } else if (document.getDocumentElement().getAttributes().getNamedItem("name").getNodeValue().equals("root")) {
            return parseIkas(document, fileName);
        } else throw new InvalidDataException(String.format(INVALID_XML, file.getName()));
    }

    private ConceptMap parseStandard(Document document, String fileName) throws InvalidDataException {
        System.out.println("Started parsing standard XML file");

        var conceptNodes = document.getElementsByTagName("concept");
        if (conceptNodes.getLength() == 0) {
            throw new InvalidDataException(String.format(MAP_NO_CONCEPTS, fileName));
        }
        var concepts = new ArrayList<Concept>();
        for (int i = 0; i < conceptNodes.getLength(); i++) {
            var node = conceptNodes.item(i);
            if (containsConcept(concepts, node.getTextContent())) {
                throw new InvalidDataException(String.format(MAP_DUPLICATE_CONCEPTS, fileName));
            }
            concepts.add(new Concept(node.getTextContent()));
        }

        var relationshipNodes = document.getElementsByTagName("relationship");
        if (relationshipNodes.getLength() == 0) {
            throw new InvalidDataException(String.format(MAP_NO_RELATIONSHIPS, fileName));
        }
        var relationships = new ArrayList<Relationship>();
        for (int i = 0; i < relationshipNodes.getLength(); i++) {
            var node = relationshipNodes.item(i);
            var from = Integer.parseInt(node.getAttributes().getNamedItem("from").getNodeValue());
            var to = Integer.parseInt(node.getAttributes().getNamedItem("to").getNodeValue());
            var fromConcept = concepts.get(from).id;
            var toConcept = concepts.get(to).id;
            relationships.add(new Relationship(fromConcept, toConcept, node.getTextContent()));
        }

        var conceptMap = new ConceptMap(concepts, relationships);
        System.out.println("Finished parsing standard XML file");
        System.out.println(conceptMap);
        return conceptMap;
    }

    private ConceptMap parseIkas(Document document, String fileName) throws InvalidDataException {
        System.out.println("Started parsing IKAS XML file");

        var concepts = new ArrayList<Concept>();
        var elementNodes = document.getElementsByTagName("element");
        for (int i = 0; i < elementNodes.getLength(); i++) {
            var node = elementNodes.item(i);
            if (node.getAttributes().getNamedItem("name").getNodeValue().equals("node")) {
                var nodeValue = node.getAttributes().getNamedItem("value").getNodeValue();
                if (containsConcept(concepts, nodeValue)) {
                    throw new InvalidDataException(String.format(MAP_DUPLICATE_CONCEPTS, fileName));
                }
                concepts.add(new Concept(nodeValue));
            }
        }
        if (concepts.isEmpty()) {
            throw new InvalidDataException(String.format(MAP_NO_CONCEPTS, fileName));
        }

        var relationships = new ArrayList<Relationship>();
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
                    throw new InvalidDataException(String.format(MAP_INVALID_RELATIONSHIP, fileName));
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
        if (relationships.isEmpty()) {
            throw new InvalidDataException(String.format(MAP_NO_RELATIONSHIPS, fileName));
        }

        var conceptMap = new ConceptMap(concepts, relationships);
        System.out.println("Finished parsing IKAS XML file");
        System.out.println(conceptMap);
        return conceptMap;
    }

    private boolean containsConcept(List<Concept> concepts, String name) {
        return concepts.stream().anyMatch(c -> c.name.compareToIgnoreCase(name) == 0);
    }
}
