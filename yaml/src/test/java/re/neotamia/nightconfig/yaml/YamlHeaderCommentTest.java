package re.neotamia.nightconfig.yaml;

import re.neotamia.nightconfig.core.CommentedConfig;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to verify YamlWriter and YamlParser correctly handle header comments
 */
public class YamlHeaderCommentTest {

    @Test
    public void testWriteAndReadHeaderComment() {
        // Create a commented config with header comment
        CommentedConfig config = CommentedConfig.inMemory();
        config.setHeaderComment("This is a header comment\nSecond line of header\nThird line of header");
        config.set("database.host", "localhost");
        config.setComment("database.host", "Database connection host");
        config.set("database.port", 5432);
        config.setComment("database.port", "Database connection port");

        // Write to YAML
        YamlWriter writer = new YamlWriter();
        StringWriter stringWriter = new StringWriter();

        System.out.println("[DEBUG_LOG] Writing config with header comment...");
        writer.write(config, stringWriter);

        String yamlString = stringWriter.toString();
        System.out.println("[DEBUG_LOG] Generated YAML with header comment:");
        System.out.println(yamlString);

        // Verify header comment is present in output
        assertTrue(yamlString.contains("# This is a header comment"), "Should contain first header comment line");
        assertTrue(yamlString.contains("# Second line of header"), "Should contain second header comment line");
        assertTrue(yamlString.contains("# Third line of header"), "Should contain third header comment line");

        // Parse the YAML back
        YamlParser parser = new YamlParser();
        CommentedConfig parsedConfig = parser.parse(new StringReader(yamlString));

        // Verify header comment is preserved
        String parsedHeaderComment = parsedConfig.getHeaderComment();
        assertNotNull(parsedHeaderComment, "Header comment should be preserved");
        assertTrue(parsedHeaderComment.contains("This is a header comment"), "Should contain first header comment line");
        assertTrue(parsedHeaderComment.contains("Second line of header"), "Should contain second header comment line");
        assertTrue(parsedHeaderComment.contains("Third line of header"), "Should contain third header comment line");

        // Verify other comments and values are preserved
        assertEquals("localhost", parsedConfig.get("database.host"));
        assertEquals(5432, parsedConfig.getInt("database.port"));
        assertEquals("Database connection host", parsedConfig.getComment("database.host"));
        assertEquals("Database connection port", parsedConfig.getComment("database.port"));
    }

    @Test
    public void testWriteAndReadSingleLineHeaderComment() {
        CommentedConfig config = CommentedConfig.inMemory();
        config.setHeaderComment("Single line header comment");
        config.set("key", "value");

        YamlWriter writer = new YamlWriter();
        StringWriter stringWriter = new StringWriter();

        writer.write(config, stringWriter);
        String yamlString = stringWriter.toString();

        System.out.println("[DEBUG_LOG] Generated YAML with single line header:");
        System.out.println(yamlString);

        // Parse back
        YamlParser parser = new YamlParser();
        CommentedConfig parsedConfig = parser.parse(new StringReader(yamlString));

        assertEquals("Single line header comment", parsedConfig.getHeaderComment());
        assertEquals("value", parsedConfig.get("key"));
    }

    @Test
    public void testNoHeaderComment() {
        CommentedConfig config = CommentedConfig.inMemory();
        config.set("key", "value");
        config.setComment("key", "Regular comment");

        YamlWriter writer = new YamlWriter();
        StringWriter stringWriter = new StringWriter();

        writer.write(config, stringWriter);
        String yamlString = stringWriter.toString();

        // Parse back
        YamlParser parser = new YamlParser();
        CommentedConfig parsedConfig = parser.parse(new StringReader(yamlString));

        assertNull(parsedConfig.getHeaderComment(), "Should have no header comment");
        assertEquals("value", parsedConfig.get("key"));
        assertEquals("Regular comment", parsedConfig.getComment("key"));
    }

    @Test
    public void testEmptyHeaderComment() {
        CommentedConfig config = CommentedConfig.inMemory();
        config.setHeaderComment("");
        config.set("key", "value");

        YamlWriter writer = new YamlWriter();
        StringWriter stringWriter = new StringWriter();

        writer.write(config, stringWriter);
        String yamlString = stringWriter.toString();

        // Parse back
        YamlParser parser = new YamlParser();
        CommentedConfig parsedConfig = parser.parse(new StringReader(yamlString));

        // Empty header comment should not be preserved
        assertNull(parsedConfig.getHeaderComment(), "Empty header comment should not be preserved");
        assertEquals("value", parsedConfig.get("key"));
    }
}
