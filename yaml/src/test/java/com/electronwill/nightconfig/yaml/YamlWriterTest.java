package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.electronwill.nightconfig.core.concurrent.StampedConfig;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class YamlWriterTest {
    @Test
    public void write() {
        Config config = YamlFormat.defaultInstance().createConfig();
        Util.populateTest(config);
        var result = new YamlWriter().writeToString(config);
        assertEquals(Util.EXPECTED_SERIALIZED, result);
    }

    @Test
    public void writeSynchronizedConfig() {
        Config config = new SynchronizedConfig(InMemoryCommentedFormat.defaultInstance(), HashMap::new);
        Util.populateTest(config);
        var result = new YamlWriter().writeToString(config);
        assertEquals(Util.EXPECTED_SERIALIZED, result);
    }

    @Test
    public void writeStampedConfig() {
        StampedConfig config = new StampedConfig(InMemoryCommentedFormat.defaultInstance(), HashMap::new);
        Util.populateTest(config);
        var result = new YamlWriter().writeToString(config);
        assertEquals(Util.EXPECTED_SERIALIZED, result);
    }

    @Test
    public void writeComments() {
        CommentedConfig config = YamlFormat.defaultInstance().createConfig();

        config.set("server.host", "localhost");
        config.set("server.port", 8080);
        config.set("database.url", "jdbc:postgresql://localhost/mydb");
        config.set("database.username", "user");
        config.set("logging.level", "INFO");

        config.setComment("server", " Server configuration");
        config.setComment("server.host", " The hostname to bind to");
        config.setComment("server.port", " The port number");
        config.setComment("database", " Database settings");
        config.setComment("database.url", " Database connection URL");
        config.setComment("database.username", " Database username");
        config.setComment("logging", " Logging configuration");
        config.setComment("logging.level", " Log level: DEBUG, INFO, WARN, ERROR");
        var result = new YamlWriter().writeToString(config);
        assertEquals(
"""
# Server configuration
server:
  # The port number
  port: '8080'
  # The hostname to bind to
  host: localhost
# Database settings
database:
  # Database connection URL
  url: jdbc:postgresql://localhost/mydb
  # Database username
  username: user
# Logging configuration
logging:
  # Log level: DEBUG, INFO, WARN, ERROR
  level: INFO
""",
                result);
    }
}
