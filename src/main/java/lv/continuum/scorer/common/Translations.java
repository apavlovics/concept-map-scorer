package lv.continuum.scorer.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.IllegalFormatException;
import java.util.Optional;
import java.util.Properties;

@Slf4j
public class Translations {

    private static final String PROPERTIES_DEFAULT = "translations/en.properties";

    private final Properties properties;

    /**
     * Initializes a new {@link Translations} instance with the data loaded from the given {@code propertiesResource}.
     *
     * @throws TranslationException if the initialization has failed.
     */
    public Translations(String propertiesResource) throws TranslationException {
        if (StringUtils.isBlank(propertiesResource)) {
            throw new TranslationException("Translation properties resource must not be blank");
        }
        try (var inputStream = getClass().getClassLoader().getResourceAsStream(propertiesResource)) {
            properties = new Properties();
            properties.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new TranslationException("Translation properties cannot be initialised", e);
        }
    }

    public Translations() throws TranslationException {
        this(PROPERTIES_DEFAULT);
    }

    public String get(String key) {
        return Optional.ofNullable(properties.getProperty(key)).orElse(key);
    }

    public String format(String key, Object... args) {
        var translation = get(key);
        try {
            return String.format(translation, args);
        } catch (IllegalFormatException e) {
            log.warn("Issue while formatting translation " + translation, e);
            return translation;
        }
    }
}
