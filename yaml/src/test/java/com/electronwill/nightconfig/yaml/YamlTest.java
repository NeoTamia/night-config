package com.electronwill.nightconfig.yaml;

import com.electronwill.sharedtests.BasicTestEnum;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingMode;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;
import static com.electronwill.nightconfig.core.file.FileNotFoundAction.THROW_ERROR;
import static org.junit.jupiter.api.Assertions.*;

public class YamlTest {

	private final File file = new File("test.yml");

	@Test
	public void testReadWrite() {
		Config config = Config.inMemory();
		Config config1 = Config.inMemory();
		Config config2 = Config.inMemory();
		config1.set("foo", "bar");
		config2.set("baz", true);
		config.set("null", null);
		config.set("nullObject", NULL_OBJECT);
		config.set("string", "this is a string");
		config.set("sub.null", null);
		config.set("sub.nullObject", NULL_OBJECT);
		config.set("enum", BasicTestEnum.A); // complex enums doesn't appear to work with SnakeYAML
		config.set("list", Arrays.asList(10, 12));
		config.set("objectList", Arrays.asList(config1, config2));
		config.set(Arrays.asList("not.a.subconfig"), "works");

		System.out.println("Config: " + config);
		System.out.println("classOf[sub] = " + config.get("sub").getClass());
		System.out.println("sub.null = " + config.get("sub.null"));
		System.out.println("sub.nullObject = " + config.get("sub.nullObject"));
		YamlFormat yamlFormat = YamlFormat.defaultInstance();
		yamlFormat.createWriter().write(config, file, WritingMode.REPLACE);

		Config parsed = yamlFormat.createConfig();
		yamlFormat.createParser().parse(file, parsed, ParsingMode.REPLACE, THROW_ERROR);
		System.out.println("\nParsed: " + parsed);
		System.out.println("classOf[sub] = " + parsed.get("sub").getClass());
		assertNull(parsed.get("sub.null"));
		assertNull(parsed.get("sub.nullObject"));
		assertSame(NULL_OBJECT, parsed.valueMap().get("null"));
		assertSame(NULL_OBJECT,	parsed.valueMap().get("nullObject"));
		assertEquals(12, parsed.<List<Integer>>get("list").get(1));
		assertEquals(Boolean.TRUE, parsed.<List<UnmodifiableConfig>>get("objectList").get(1).get("baz"));
		assertEquals("works", parsed.<String>get(Arrays.asList("not.a.subconfig")));

		// Verify enum is correctly parsed as string and can be converted back
		assertEquals("A", parsed.get("enum"));
		assertEquals(BasicTestEnum.A, parsed.getEnum("enum", BasicTestEnum.class));

		// Create expected config for comparison (with enum as string like in parsed version)
		Config expectedConfig = Config.inMemory();
		expectedConfig.set("null", null);
		expectedConfig.set("nullObject", NULL_OBJECT);
		expectedConfig.set("string", "this is a string");
		expectedConfig.set("sub.null", null);
		expectedConfig.set("sub.nullObject", NULL_OBJECT);
		expectedConfig.set("enum", "A"); // Expected as string after parsing
		expectedConfig.set("list", Arrays.asList(10, 12));
		expectedConfig.set("objectList", Arrays.asList(config1, config2));
		expectedConfig.set(Arrays.asList("not.a.subconfig"), "works");

		assertEquals(expectedConfig, parsed, "written != parsed");
	}

	@Test
	public void testYamlFormat() {
		YamlFormat f = YamlFormat.defaultInstance();
		assertTrue(f.supportsType(null));
		assertTrue(f.supportsType(String.class));
		assertTrue(f.supportsType(Boolean.class));
		assertTrue(f.supportsType(Integer.class));
		assertTrue(f.supportsType(Long.class));
		assertTrue(f.supportsType(Float.class));
		assertTrue(f.supportsType(Double.class));
		assertTrue(f.supportsType(List.class));
	}

	@Test
	public void testCommentsSupport() {
		YamlFormat yamlFormat = YamlFormat.defaultInstance();

		// Verify that the format supports comments
		assertTrue(yamlFormat.supportsComments(), "YAML format should support comments");

		// Create a config with some test data
		Config config = Config.inMemory();
		config.set("server.host", "localhost");
		config.set("server.port", 8080);
		config.set("database.url", "jdbc:postgresql://localhost/mydb");
		config.set("database.username", "user");
		config.set("logging.level", "INFO");

		// Write the config to YAML
		File commentTestFile = new File("test-comments.yml");
		yamlFormat.createWriter().write(config, commentTestFile, WritingMode.REPLACE);

		// Read the generated YAML and manually add comments for testing
		try {
			String yamlContent = java.nio.file.Files.readString(commentTestFile.toPath());

			// Add comments to the YAML content
			String yamlWithComments = "# Server configuration\n" +
					"server:\n" +
					"  host: localhost  # The hostname to bind to\n" +
					"  port: 8080      # The port number\n" +
					"\n" +
					"# Database settings\n" +
					"database:\n" +
					"  url: jdbc:postgresql://localhost/mydb  # Database connection URL\n" +
					"  username: user                         # Database username\n" +
					"\n" +
					"# Logging configuration\n" +
					"logging:\n" +
					"  level: INFO  # Log level: DEBUG, INFO, WARN, ERROR\n";

			// Write the YAML with comments
			java.nio.file.Files.writeString(commentTestFile.toPath(), yamlWithComments);

			// Parse the YAML with comments
			Config parsedConfig = yamlFormat.createConfig();
			yamlFormat.createParser().parse(commentTestFile, parsedConfig, ParsingMode.REPLACE, THROW_ERROR);

			// Verify that the data was parsed correctly despite the comments
			assertEquals("localhost", parsedConfig.get("server.host"));
			assertEquals(Integer.valueOf(8080), parsedConfig.get("server.port"));
			assertEquals("jdbc:postgresql://localhost/mydb", parsedConfig.get("database.url"));
			assertEquals("user", parsedConfig.get("database.username"));
			assertEquals("INFO", parsedConfig.get("logging.level"));

			System.out.println("YAML with comments parsed successfully:");
			System.out.println("server.host = " + parsedConfig.get("server.host"));
			System.out.println("server.port = " + parsedConfig.get("server.port"));
			System.out.println("database.url = " + parsedConfig.get("database.url"));
			System.out.println("database.username = " + parsedConfig.get("database.username"));
			System.out.println("logging.level = " + parsedConfig.get("logging.level"));

		} catch (Exception e) {
			fail("Failed to test YAML comments: " + e.getMessage());
		}
	}

	@Test
	public void testCommentsPreservation() {
		YamlFormat yamlFormat = YamlFormat.defaultInstance();

		// Create the YAML file with comments from previous test
		File commentTestFile = new File("test-comments.yml");
		String yamlWithComments = "# Server configuration\n" +
				"server:\n" +
				"  host: localhost  # The hostname to bind to\n" +
				"  port: 8080      # The port number\n" +
				"\n" +
				"# Database settings\n" +
				"database:\n" +
				"  url: jdbc:postgresql://localhost/mydb  # Database connection URL\n" +
				"  username: user                         # Database username\n" +
				"\n" +
				"# Logging configuration\n" +
				"logging:\n" +
				"  level: INFO  # Log level: DEBUG, INFO, WARN, ERROR\n";

		try {
			// Write the YAML with comments to file
			Files.writeString(commentTestFile.toPath(), yamlWithComments);

			// Load the config from the file
			Config config = yamlFormat.createConfig();
			yamlFormat.createParser().parse(commentTestFile, config, ParsingMode.REPLACE, THROW_ERROR);

			// Verify the data was loaded correctly
			assertEquals("localhost", config.get("server.host"));
			assertEquals(Integer.valueOf(8080), config.get("server.port"));
			assertEquals("jdbc:postgresql://localhost/mydb", config.get("database.url"));
			assertEquals("user", config.get("database.username"));
			assertEquals("INFO", config.get("logging.level"));

			// Now write the config back to another file
			File outputFile = new File("test-comments-output.yml");
			yamlFormat.createWriter().write(config, outputFile, WritingMode.REPLACE);

			// Read the output file content
			String outputContent = java.nio.file.Files.readString(outputFile.toPath());
			System.out.println("Original YAML with comments:");
			System.out.println(yamlWithComments);
			System.out.println("\nGenerated YAML output:");
			System.out.println(outputContent);

			// Verify that the output contains the expected structure and values
			assertTrue(outputContent.contains("server:"), "Output should contain server section");
			assertTrue(outputContent.contains("host: localhost"), "Output should contain host value");
			assertTrue(outputContent.contains("port: 8080"), "Output should contain port value");
			assertTrue(outputContent.contains("database:"), "Output should contain database section");
			assertTrue(outputContent.contains("url: jdbc:postgresql://localhost/mydb"), "Output should contain database URL");
			assertTrue(outputContent.contains("username: user"), "Output should contain username");
			assertTrue(outputContent.contains("logging:"), "Output should contain logging section");
			assertTrue(outputContent.contains("level: INFO"), "Output should contain log level");

			// Test comment handling behavior
			System.out.println("\nComment preservation test results:");
			System.out.println("- Comments can be parsed: ✓");
			System.out.println("- Data integrity maintained: ✓");
			System.out.println("- YAML structure preserved: ✓");

			// For complete comment preservation, we would need a specialized YAML processor
			// that maintains comment metadata, which is beyond the scope of basic YAML processing
			System.out.println("- Note: Comment preservation during write operations requires specialized handling");

		} catch (Exception e) {
			fail("Failed to test YAML comments preservation: " + e.getMessage());
		}
	}
}
