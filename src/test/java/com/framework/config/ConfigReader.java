package com.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads framework configuration from src/test/resources/config.properties.
 * Keep environment-specific values (URLs, credentials, timeouts) here instead
 * of hardcoding them in page objects or step definitions.
 */
public final class ConfigReader {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IllegalStateException("config.properties not found on classpath");
            }
            PROPERTIES.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config.properties", e);
        }
    }

    private ConfigReader() {
    }

    public static String get(String key) {
        // System property overrides the file, e.g. -Dbrowser=firefox
        String override = System.getProperty(key);
        return override != null ? override : PROPERTIES.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    public static String browser() {
        return get("browser", "chrome");
    }

    public static String baseUrl() {
        return get("base.url");
    }

    public static boolean headless() {
        return Boolean.parseBoolean(get("headless", "true"));
    }

    public static int implicitWaitSeconds() {
        return Integer.parseInt(get("implicit.wait", "10"));
    }
}
