package lv.continuum.scorer.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TranslationsTest {

    private final Translations translations = new Translations("translations/test.properties");

    @Test
    void constructInvalid() {
        String[] invalidPropertiesPaths = {"invalid.properties", "", null};
        for (var invalidPropertiesPath : invalidPropertiesPaths) {
            assertThrows(TranslationException.class, () -> new Translations(invalidPropertiesPath));
        }
    }

    @Test
    void get() {
        assertEquals("Known Value", translations.get("known-key"));
        assertEquals("unknown-key", translations.get("unknown-key"));
    }
}
