package lv.continuum.scorer.logic;

import lv.continuum.scorer.TestData;
import lv.continuum.scorer.common.InvalidDataException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConceptMapParserTest {

    private final TestData data = TestData.getInstance();
    private final ConceptMapParser conceptMapParser = new ConceptMapParser();

    private String makeFilePath(String fileName) {
        return "src/test/resources/samples/" + fileName;
    }

    @Test
    void parseStandard() throws ParserConfigurationException, InvalidDataException, SAXException, IOException {
        assertEquals(data.conceptMapWithLevels, conceptMapParser.parse(makeFilePath("concept-map-with-levels.xml")));
    }

    @Test
    void parseIkas() throws ParserConfigurationException, InvalidDataException, SAXException, IOException {
        assertEquals(data.conceptMapWithCycles, conceptMapParser.parse(makeFilePath("concept-map-with-cycles.xml")));
    }
}
