package re.neotamia.nightconfig.toml;

import org.junit.jupiter.api.Test;
import re.neotamia.nightconfig.core.CommentedConfig;

import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

public class TomlHeaderCommentTest {
    @Test
    public void testWriteAndReadHeaderComment() {
        CommentedConfig config = CommentedConfig.inMemory();
        config.setHeaderComment("This is a header comment\nSecond line of header\nThird line of header");
        config.set("database.host", "localhost");
        config.setComment("database.host", "Database connection host");
        config.set("database.port", 5432);
        config.setComment("database.port", "Database connection port");

        TomlWriter writer = new TomlWriter();
        StringWriter stringWriter = new StringWriter();

        System.out.println("[DEBUG_LOG] Writing config with header comment...");
        writer.write(config, stringWriter);

        String tomlString = stringWriter.toString();
        System.out.println("[DEBUG_LOG] Generated TOML with header comment:");
        System.out.println(tomlString);

        assertTrue(tomlString.contains("# This is a header comment"), "Should contain first header comment line");
        assertTrue(tomlString.contains("# Second line of header"), "Should contain second header comment line");
        assertTrue(tomlString.contains("# Third line of header"), "Should contain third header comment line");

        // Parse the TOML back
        TomlParser parser = new TomlParser();
        CommentedConfig parsedConfig = parser.parse(new StringReader(tomlString));

        // Verify header comment is preserved
        String parsedHeaderComment = parsedConfig.getHeaderComment();
        assertNotNull(parsedHeaderComment, "Header comment should be preserved");
        assertTrue(parsedHeaderComment.contains("This is a header comment"), "Should contain first header comment line");
        assertTrue(parsedHeaderComment.contains("Second line of header"), "Should contain second header comment line");
        assertTrue(parsedHeaderComment.contains("Third line of header"), "Should contain third header comment line");
        assertNull(parsedConfig.getComment("database"), "No comment should be associated with the 'database' table itself");

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

        TomlWriter writer = new TomlWriter();
        StringWriter stringWriter = new StringWriter();

        writer.write(config, stringWriter);
        String tomlString = stringWriter.toString();

        System.out.println("[DEBUG_LOG] Generated TOML with single line header:");
        System.out.println(tomlString);

        // Parse back
        TomlParser parser = new TomlParser();
        CommentedConfig parsedConfig = parser.parse(new StringReader(tomlString));

        assertEquals("Single line header comment", parsedConfig.getHeaderComment());
        assertNull(parsedConfig.getComment("key"), "No comment should be associated with 'key'");
        assertEquals("value", parsedConfig.get("key"));
    }

    @Test
    public void testNoHeaderComment() {
        CommentedConfig config = CommentedConfig.inMemory();
        config.set("key", "value");
        config.setComment("key", "Regular comment");

        TomlWriter writer = new TomlWriter();
        StringWriter stringWriter = new StringWriter();

        writer.write(config, stringWriter);
        String tomlString = stringWriter.toString();

        // Parse back
        TomlParser parser = new TomlParser();
        CommentedConfig parsedConfig = parser.parse(new StringReader(tomlString));

        assertNull(parsedConfig.getHeaderComment(), "Should have no header comment");
        assertEquals("value", parsedConfig.get("key"));
        assertEquals("Regular comment", parsedConfig.getComment("key"));
    }

    @Test
    public void testEmptyHeaderComment() {
        CommentedConfig config = CommentedConfig.inMemory();
        config.setHeaderComment("");
        config.set("key", "value");

        TomlWriter writer = new TomlWriter();
        StringWriter stringWriter = new StringWriter();

        writer.write(config, stringWriter);
        String tomlString = stringWriter.toString();

        // Parse back
        TomlParser parser = new TomlParser();
        CommentedConfig parsedConfig = parser.parse(new StringReader(tomlString));

        // Empty header comment should not be preserved
        assertNull(parsedConfig.getHeaderComment(), "Empty header comment should not be preserved");
        assertEquals("value", parsedConfig.get("key"));
    }
}
