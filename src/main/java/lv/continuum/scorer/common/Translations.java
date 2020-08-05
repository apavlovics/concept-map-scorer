package lv.continuum.scorer.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;

public class Translations {

    private static final String PROPERTIES_PATH = "translations/en.properties";
    private static final Properties properties = new Properties();
    private static Translations instance;

    private Translations() {
        try {
            var file = getClass().getClassLoader().getResource(PROPERTIES_PATH).getFile();
            try (var fis = new FileInputStream(file)) {
                properties.load(new InputStreamReader(fis, StandardCharsets.UTF_8));
            }
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException("Translation properties cannot be initialised", e);
        }
    }

    /**
     * Initializes and provides access to the {@link Translations} singleton.
     *
     * @return the {@link Translations} singleton.
     * @throws IllegalStateException if initialization has failed.
     */
    public static Translations getInstance() {
        if (instance == null) {
            synchronized (Translations.class) {
                if (instance == null) instance = new Translations();
            }
        }
        return instance;
    }

    public String get(String key) {
        return Optional.ofNullable(properties.getProperty(key)).orElse(key);
    }
}
