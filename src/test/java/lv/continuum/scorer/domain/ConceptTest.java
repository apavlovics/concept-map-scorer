package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.InvalidDataException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConceptTest {

    @Test
    void constructValid() throws InvalidDataException {
        new Concept("name");
    }

    @Test
    void constructInvalid() {
        String[] invalidNames = {"", " ", null};
        for (var invalidName : invalidNames) {
            assertThrows(InvalidDataException.class, () -> new Concept(invalidName));
        }
    }

    @Test
    void deriveId() {
        var namesToIds = Map.of("Name", "name", "Another Name", "another-name", " Piemērs ", "piemērs");
        namesToIds.forEach((name, id) -> assertEquals(id, Concept.deriveId(name)));
    }
}
