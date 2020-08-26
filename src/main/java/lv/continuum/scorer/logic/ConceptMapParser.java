package lv.continuum.scorer.logic;

import lombok.extern.slf4j.Slf4j;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

@Slf4j
public class ConceptMapParser {

    private static final Translations translations = Translations.getInstance();

    public ConceptMap parse(String conceptMapPath)
            throws IOException, ParserConfigurationException, SAXException, InvalidDataException {
        var file = new File(conceptMapPath);
        var document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        document.getDocumentElement().normalize();

        try {
            var fileName = file.getName();
            if (document.getDocumentElement().getNodeName().equals("conceptmap")) {
                return parseStandard(document, fileName);
            } else if (document.getDocumentElement().getAttributes().getNamedItem("name").getNodeValue().equals("root")) {
                return parseIkas(document, fileName);
            } else {
                throw new InvalidDataException(String.format(translations.get("invalid-xml"), file.getName()));
            }
        } catch (NumberFormatException | NullPointerException e) {
            throw new InvalidDataException(String.format(translations.get("invalid-xml"), file.getName()));
        }
    }

    private ConceptMap parseStandard(Document document, String fileName) throws InvalidDataException {
        log.debug("Started parsing standard XML file");

        var conceptNodes = document.getElementsByTagName("concept");
        var concepts = new HashMap<Integer, Concept>();
        for (var i = 0; i < conceptNodes.getLength(); i++) {
            var node = conceptNodes.item(i);
            var name = node.getTextContent();
            if (hasDuplicateConcept(concepts.values(), name)) {
                throw new InvalidDataException(String.format(translations.get("concept-map-duplicate-concepts"), fileName));
            }
            var documentId = Integer.parseInt(node.getAttributes().getNamedItem("id").getNodeValue());
            concepts.put(documentId, new Concept(name));
        }

        var relationshipNodes = document.getElementsByTagName("relationship");
        var relationships = new HashSet<Relationship>();
        for (var i = 0; i < relationshipNodes.getLength(); i++) {
            var node = relationshipNodes.item(i);
            var fromDocumentId = Integer.parseInt(node.getAttributes().getNamedItem("from").getNodeValue());
            var toDocumentId = Integer.parseInt(node.getAttributes().getNamedItem("to").getNodeValue());
            var fromConcept = concepts.get(fromDocumentId);
            var toConcept = concepts.get(toDocumentId);
            relationships.add(new Relationship(fromConcept, toConcept, node.getTextContent()));
        }

        var conceptMap = new ConceptMap(new HashSet<>(concepts.values()), relationships, fileName);
        log.debug("Finished parsing standard XML file\n{}", conceptMap);
        return conceptMap;
    }

    private ConceptMap parseIkas(Document document, String fileName) throws InvalidDataException {
        log.debug("Started parsing IKAS XML file");

        var concepts = new HashMap<String, Concept>();
        var elementNodes = document.getElementsByTagName("element");
        for (var i = 0; i < elementNodes.getLength(); i++) {
            var node = elementNodes.item(i);
            if (node.getAttributes().getNamedItem("name").getNodeValue().equals("node")) {
                var name = node.getAttributes().getNamedItem("value").getNodeValue();
                if (hasDuplicateConcept(concepts.values(), name)) {
                    throw new InvalidDataException(String.format(translations.get("concept-map-duplicate-concepts"), fileName));
                }
                concepts.put(name, new Concept(name));
            }
        }

        var relationships = new HashSet<Relationship>();
        for (var i = 0; i < elementNodes.getLength(); i++) {
            var node = elementNodes.item(i);
            if (node.getAttributes().getNamedItem("name").getNodeValue().equals("relation")) {
                String fromConceptName = null, toConceptName = null;
                var nodeValue = node.getAttributes().getNamedItem("value").getNodeValue();
                var relationshipNodes = ((Element) node).getElementsByTagName("element");
                for (var j = 0; j < relationshipNodes.getLength(); j++) {
                    var relationshipNode = relationshipNodes.item(j);
                    var relationshipNodeName = relationshipNode.getAttributes().getNamedItem("name").getNodeValue();
                    var relationshipNodeValue = relationshipNode.getAttributes().getNamedItem("value").getNodeValue();
                    if (relationshipNodeName.equals("source")) {
                        fromConceptName = relationshipNodeValue;
                    } else if (relationshipNodeName.equals("target")) {
                        toConceptName = relationshipNodeValue;
                    }
                }
                if (fromConceptName == null || toConceptName == null) {
                    throw new InvalidDataException(String.format(translations.get("concept-map-invalid-relationship"), fileName));
                }

                var fromConcept = concepts.get(fromConceptName);
                var toConcept = concepts.get(toConceptName);
                relationships.add(new Relationship(fromConcept, toConcept, nodeValue));
            }
        }

        var conceptMap = new ConceptMap(new HashSet<>(concepts.values()), relationships, fileName);
        log.debug("Finished parsing IKAS XML file\n{}", conceptMap);
        return conceptMap;
    }

    private boolean hasDuplicateConcept(Collection<Concept> concepts, String name) {
        var id = Concept.deriveId(name);
        return concepts.stream().anyMatch(c -> c.id.equals(id));
    }
}
