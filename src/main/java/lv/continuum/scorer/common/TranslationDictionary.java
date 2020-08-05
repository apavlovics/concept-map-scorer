package lv.continuum.scorer.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class TranslationDictionary {

    private static final String PROPERTIES_PATH = "translations/en.properties";
    private static final Properties PROPERTIES = new Properties();

    private static TranslationDictionary instance;

    private TranslationDictionary() {
        try {
            var file = getClass().getClassLoader().getResource(PROPERTIES_PATH).getFile();
            try (var fis = new FileInputStream(file)) {
                PROPERTIES.load(new InputStreamReader(fis, StandardCharsets.UTF_8));
            }
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException("Translation properties cannot be initialised", e);
        }
    }

    /**
     * Initializes and provides access to the {@link TranslationDictionary} singleton.
     *
     * @return the {@link TranslationDictionary} singleton.
     * @throws IllegalStateException if initialization has failed.
     */
    public static TranslationDictionary getInstance() {
        if (instance == null) {
            synchronized (TranslationDictionary.class) {
                if (instance == null) instance = new TranslationDictionary();
            }
        }
        return instance;
    }

    public String getTranslation(String key) {
        var value = PROPERTIES.getProperty(key);
        if (value == null) value = key;
        return value;
    }
}
