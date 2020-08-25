package lv.continuum.scorer.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TranslationsTest {

    private final Translations translations = new Translations("translations/test.properties");

    @Test
    void get() {
        assertEquals("Known Value", translations.get("known-key"));
        assertEquals("unknown-key", translations.get("unknown-key"));
    }
}
