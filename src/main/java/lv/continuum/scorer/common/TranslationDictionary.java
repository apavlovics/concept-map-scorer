package lv.continuum.scorer.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class TranslationDictionary {

    private static final String PROPERTIES_PATH = "properties/translation.properties";
    private static final Properties properties = new Properties();

    private static TranslationDictionary instance;

    protected TranslationDictionary() {
        try {
            var file = getClass().getClassLoader().getResource(PROPERTIES_PATH).getFile();
            try (var fis = new FileInputStream(file)) {
                properties.load(fis);
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
            instance = new TranslationDictionary();
        }
        return instance;
    }

    // TODO Add @NotNull to key
    public String getTranslation(String key) {
        var value = properties.getProperty(key);
        if (value == null) {
            value = key;
        }
        return value;
    }
}
