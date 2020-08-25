package lv.continuum.scorer.common;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;

public class Translations {

    private static final String PROPERTIES_PATH = "translations/en.properties";

    private final Properties properties;

    Translations(String propertiesPath) throws TranslationException {
        if (StringUtils.isBlank(propertiesPath)) {
            throw new TranslationException("Translation properties path must not be blank");
        }
        try (var inputStream = getClass().getClassLoader().getResourceAsStream(propertiesPath)) {
            properties = new Properties();
            properties.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new TranslationException("Translation properties cannot be initialised", e);
        }
    }

    private static Translations instance;

    /**
     * Initializes and provides access to the {@link Translations} singleton.
     *
     * @return the {@link Translations} singleton.
     * @throws TranslationException if initialization has failed.
     */
    public static Translations getInstance() throws TranslationException {
        if (instance == null) {
            synchronized (Translations.class) {
                if (instance == null) instance = new Translations(PROPERTIES_PATH);
            }
        }
        return instance;
    }

    public String get(String key) {
        return Optional.ofNullable(properties.getProperty(key)).orElse(key);
    }
}
