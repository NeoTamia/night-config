package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.CommentedConfig;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class to verify YamlWriter correctly handles comments from UnmodifiableCommentedConfig
 */
public class YamlWriterCommentsTest {

    @Test
    public void testWriteWithComments() {
        // Create a commented config
        CommentedConfig config = CommentedConfig.inMemory();
        config.set("database.host", "localhost");
        config.setComment("database.host", "Database connection host");
        config.set("database.port", 5432);
        config.setComment("database.port", "Database connection port");
        config.set("app.name", "MyApp");
        config.setComment("app.name", "Application name");
        config.set("app.version", "1.0.0");
        config.setComment("app.version", "Application version");

        // Create nested config with comments
        CommentedConfig nested = config.createSubConfig();
        nested.set("timeout", 30);
        nested.setComment("timeout", "Connection timeout in seconds");
        config.set("settings", nested);
        config.setComment("settings", "Application settings");

        // Write to YAML
        YamlWriter writer = new YamlWriter();
        StringWriter stringWriter = new StringWriter();

        System.out.println("[DEBUG_LOG] Writing config with comments...");
        writer.write(config, stringWriter);

        String result = stringWriter.toString();
        System.out.println("[DEBUG_LOG] Generated YAML:");
        System.out.println(result);

        // Verify comments are present
        assertTrue(result.contains("# Database connection host"), "Should contain database host comment");
        assertTrue(result.contains("# Database connection port"), "Should contain database port comment");
        assertTrue(result.contains("# Application name"), "Should contain app name comment");
        assertTrue(result.contains("# Application version"), "Should contain app version comment");
        assertTrue(result.contains("# Application settings"), "Should contain settings comment");
        assertTrue(result.contains("# Connection timeout in seconds"), "Should contain timeout comment");

        // Verify values are present
        assertTrue(result.contains("localhost"), "Should contain localhost value");
        assertTrue(result.contains("5432"), "Should contain port value");
        assertTrue(result.contains("MyApp"), "Should contain app name value");
        assertTrue(result.contains("1.0.0"), "Should contain version value");
        assertTrue(result.contains("30"), "Should contain timeout value");
    }

    @Test
    public void testWriteWithMultilineComments() {
        CommentedConfig config = CommentedConfig.inMemory();
        config.set("key", "value");
        config.setComment("key", "This is a multiline comment\nSecond line of comment\nThird line of comment");

        YamlWriter writer = new YamlWriter();
        StringWriter stringWriter = new StringWriter();

        System.out.println("[DEBUG_LOG] Writing config with multiline comments...");
        writer.write(config, stringWriter);

        String result = stringWriter.toString();
        System.out.println("[DEBUG_LOG] Generated YAML with multiline comments:");
        System.out.println(result);

        // Verify all comment lines are present
        assertTrue(result.contains("# This is a multiline comment"), "Should contain first comment line");
        assertTrue(result.contains("# Second line of comment"), "Should contain second comment line");
        assertTrue(result.contains("# Third line of comment"), "Should contain third comment line");
        assertTrue(result.contains("value"), "Should contain the value");
    }

    @Test
    public void testWriteWithoutComments() {
        CommentedConfig config = CommentedConfig.inMemory();
        config.set("simple", "value");
        config.set("number", 42);

        YamlWriter writer = new YamlWriter();
        StringWriter stringWriter = new StringWriter();

        System.out.println("[DEBUG_LOG] Writing config without comments...");
        writer.write(config, stringWriter);

        String result = stringWriter.toString();
        System.out.println("[DEBUG_LOG] Generated YAML without comments:");
        System.out.println(result);

        // Verify values are present but no comments
        assertTrue(result.contains("value"), "Should contain simple value");
        assertTrue(result.contains("42"), "Should contain number value");
        assertFalse(result.contains("#"), "Should not contain any comment markers");
    }
}
