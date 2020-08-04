package lv.continuum.scorer.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Andrey Pavlovich
 */
public class TranslationDictionary {
    final public static String TRANSLATION_PROPERTIES_PATH = "properties/translation.properties";
    final public static String ERROR = "Error";
    final public static String TRANSLATION_ERROR = "Translation error";
    final public static String ERROR_TEXT = "Translation properties cannot be initialised.";

    private static Properties translationProperties;
    private static TranslationDictionary instance = null;

    protected TranslationDictionary() throws IOException {
        translationProperties = new Properties();
        FileInputStream fis = new FileInputStream(getClass().getClassLoader().getResource(TRANSLATION_PROPERTIES_PATH).getFile());
        translationProperties.load(fis);
        fis.close();
    }

    /**
     * Provides access to the TranslationDictionary instance.
     * @return the TranslationDictionary instance or null, if initialisation has failed
     */
    public static TranslationDictionary getInstance() {
        if (instance == null) {
            try {
                instance = new TranslationDictionary();
            } catch (Exception e) {
                System.out.println(ERROR_TEXT);
                return null;
            }
        }
    return instance;
    }

    public String getTranslation(String key) {
        String value;
        value = translationProperties.getProperty(key);
        if (value != null) return value;
        value = translationProperties.getProperty("default");
        if (value != null) return value;
        return TRANSLATION_ERROR;
    }
}
