package org.example.config;

import java.io.InputStream;
import java.util.Properties;

public class BotConfig {
    private final Properties props = new Properties();

    public BotConfig() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            props.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getToken() {
        return props.getProperty("bot.token");
    }

    public String getUsername() {
        return props.getProperty("bot.username");
    }

    public String getDbPath() {
        return props.getProperty("db.path");
    }
}
