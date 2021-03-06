package lv.continuum.scorer.logic;

import lv.continuum.scorer.TestData;
import lv.continuum.scorer.common.InvalidDataException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConceptMapParserTest {

    private final TestData data = TestData.getInstance();
    private final ConceptMapParser conceptMapParser = new ConceptMapParser();

    private String makePath(String fileName) {
        return "src/test/resources/samples/" + fileName;
    }

    @Test
    void parseStandard() throws Exception {
        assertEquals(
                data.conceptMapWithLevels,
                conceptMapParser.parse(makePath(data.fileNameWithLevels))
        );
    }

    @Test
    void parseIkas() throws Exception {
        assertEquals(
                data.conceptMapWithCycles,
                conceptMapParser.parse(makePath(data.fileNameWithCycles))
        );
    }

    @Test
    void parseInvalid() {
        assertThrows(
                InvalidDataException.class,
                () -> conceptMapParser.parse(makePath(data.fileNameInvalid))
        );
        assertThrows(
                IOException.class,
                () -> conceptMapParser.parse(makePath(data.fileNameNotFound))
        );
    }
}
