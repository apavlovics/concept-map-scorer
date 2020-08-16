package lv.continuum.scorer.domain;

import lv.continuum.scorer.common.InvalidDataException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConceptTest {

    @Test
    public void constructValid() throws InvalidDataException {
        new Concept("name");
    }

    @Test
    public void constructInvalid() {
        String[] invalidNames = {"", " ", null};
        for (var invalidName : invalidNames) {
            assertThrows(InvalidDataException.class, () -> new Concept(invalidName));
        }
    }
}
