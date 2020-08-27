package lv.continuum.scorer.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TranslationsTest {

    private static final String PROPERTIES_TEST = "translations/test.properties";

    private final Translations translations = new Translations(PROPERTIES_TEST);

    @Test
    void constructInvalid() {
        String[] invalidPropertiesPaths = {"invalid.properties", "", null};
        for (var invalidPropertiesPath : invalidPropertiesPaths) {
            assertThrows(TranslationException.class, () -> new Translations(invalidPropertiesPath));
        }
    }

    @Test
    void get() {
        assertEquals("Simple translation", translations.get("key-simple"));
        assertEquals("key-not-found", translations.get("key-not-found"));
    }

    @Test
    void formatValid() {
        assertEquals("Perfect translation", translations.format("key-formatted", "Perfect"));
    }

    @Test
    void formatInvalid() {
        assertEquals("%s translation", translations.format("key-formatted"));
        assertEquals("key-not-found", translations.format("key-not-found", 123));
    }
}
