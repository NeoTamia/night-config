package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.CommentedConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.electronwill.nightconfig.core.concurrent.StampedConfig;
import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.ParsingMode;

import java.io.File;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class YamlParserTest {
    @Test
    public void read() {
        Config config = new YamlParser().parse(new File("test.yml"), FileNotFoundAction.THROW_ERROR);
		Util.checkExample(config);
    }

    @Test
	public void readToSynchronizedConfig() {
		File f = new File("test.yml");
		SynchronizedConfig config = new SynchronizedConfig(InMemoryCommentedFormat.defaultInstance(), HashMap::new);
		new YamlParser().parse(f, config, ParsingMode.REPLACE, FileNotFoundAction.THROW_ERROR);
		Util.checkExample(config);
	}

	@Test
	public void readToStampedConfig() {
		File f = new File("test.yml");
		StampedConfig config = new StampedConfig(InMemoryCommentedFormat.defaultInstance(), HashMap::new);
		new YamlParser().parse(f, config, ParsingMode.REPLACE, FileNotFoundAction.THROW_ERROR);
		Util.checkExample(config);
	}

    @Test
    public void readToCommentedConfig() {
        CommentedConfig config = new YamlParser().parse(
"""
# Server configuration
server:
  # The hostname to bind to
  host: localhost
  # The port number
  port: 8080

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
"""
        );
        assertEquals("Server configuration", config.getComment("server"));
        assertEquals("The hostname to bind to", config.getComment("server.host"));
        assertEquals("localhost", config.get("server.host"));
        assertEquals("The port number", config.getComment("server.port"));
        assertEquals(8080, config.getInt("server.port"));
        assertEquals("Database settings", config.getComment("database"));
        assertEquals("Database connection URL", config.getComment("database.url"));
        assertEquals("jdbc:postgresql://localhost/mydb", config.get("database.url"));
        assertEquals("Database username", config.getComment("database.username"));
        assertEquals("user", config.get("database.username"));
        assertEquals("Logging configuration", config.getComment("logging"));
        assertEquals("Log level: DEBUG, INFO, WARN, ERROR", config.getComment("logging.level"));
        assertEquals("INFO", config.get("logging.level"));
    }
}
