import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import re.neotamia.nightconfig.core.Config;
import re.neotamia.nightconfig.core.file.CommentedFileConfig;
import re.neotamia.nightconfig.core.file.FileConfig;
import re.neotamia.nightconfig.core.file.FormatDetector;
import re.neotamia.nightconfig.core.serde.*;
import re.neotamia.nightconfig.json.JsonFormat;
import re.neotamia.nightconfig.toml.TomlFormat;
import re.neotamia.nightconfig.yaml.YamlFormat;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TypeAdapter} with different config formats (YAML, JSON,
 * TOML).
 * Verifies that TypeAdapter works correctly when serializing to and
 * deserializing from files.
 */
public class TypeAdapterFormatTest {

    @TempDir
    Path tempDir;

    @BeforeAll
    public static void setup() {
        FormatDetector.registerExtension("yaml", YamlFormat.defaultInstance());
        FormatDetector.registerExtension("yml", YamlFormat.defaultInstance());
        FormatDetector.registerExtension("json", JsonFormat.fancyInstance());
        FormatDetector.registerExtension("toml", TomlFormat.instance());
    }

    // ============ Test Data Classes ============

    /**
     * A simple generic container class.
     */
    public static class Box<T> {
        private T value;

        public Box() {
        }

        public Box(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Box{" + value + '}';
        }
    }

    /**
     * An enum for testing.
     */
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    /**
     * A config class with Box fields and List<Box<T>> for testing.
     */
    public static class TaskConfig {
        public Box<String> taskName = new Box<>("Unnamed Task");
        public Box<Integer> taskId = new Box<>(0);
        public Box<Priority> priority = new Box<>(Priority.MEDIUM);
        public List<Box<String>> tags = List.of();
        public List<Box<Priority>> previousPriorities = List.of();

        public TaskConfig() {
        }
    }

    // ============ Generic BoxTypeAdapter ============

    /**
     * A generic TypeAdapter for Box<T>.
     */
    public static class BoxTypeAdapter<T> implements TypeAdapter<Box<T>, Object> {

        @Override
        public boolean canHandle(Type type) {
            if (type instanceof ParameterizedType pt) {
                return pt.getRawType() == Box.class;
            }
            return type == Box.class;
        }

        @Override
        public Object serialize(Box<T> value, Type type, SerializerContext ctx) {
            return ctx.serializeValue(value.getValue());
        }

        @Override
        @SuppressWarnings("unchecked")
        public Box<T> deserialize(Object value, Type type, DeserializerContext ctx) {
            Type valueType = Object.class;
            if (type instanceof ParameterizedType pt) {
                Type[] typeArgs = pt.getActualTypeArguments();
                if (typeArgs.length > 0) {
                    valueType = typeArgs[0];
                }
            }
            T innerValue = (T) ctx.deserializeValue(value, new TypeConstraint(valueType));
            return new Box<>(innerValue);
        }
    }

    // ============ YAML Format Tests ============

    @Test
    public void testTypeAdapter_YamlFormat_SerializeAndDeserialize() throws IOException {
        Path yamlPath = tempDir.resolve("task-config.yaml");

        // Create serializer and deserializer with TypeAdapter
        ObjectSerializer serializer = ObjectSerializer.builder()
                .withTypeAdapter(new BoxTypeAdapter<>())
                .build();
        ObjectDeserializer deserializer = ObjectDeserializer.builder()
                .withTypeAdapter(new BoxTypeAdapter<>())
                .build();

        // Create config object
        TaskConfig original = new TaskConfig();
        original.taskName = new Box<>("Build Feature X");
        original.taskId = new Box<>(12345);
        original.priority = new Box<>(Priority.HIGH);
        original.tags = List.of(new Box<>("backend"), new Box<>("urgent"), new Box<>("sprint-1"));
        original.previousPriorities = List.of(new Box<>(Priority.LOW), new Box<>(Priority.MEDIUM));

        // Serialize to YAML file
        try (CommentedFileConfig config = CommentedFileConfig.builder(yamlPath).sync().build()) {
            serializer.serializeFields(original, config);
            config.save();
        }

        // Verify file exists and has content
        assertTrue(Files.exists(yamlPath), "YAML file should exist");
        String yamlContent = Files.readString(yamlPath);
        assertTrue(yamlContent.contains("Build Feature X"), "YAML should contain task name");
        assertTrue(yamlContent.contains("12345"), "YAML should contain task ID");
        assertTrue(yamlContent.contains("HIGH"), "YAML should contain priority");

        // Deserialize from YAML file
        TaskConfig restored;
        try (CommentedFileConfig config = CommentedFileConfig.builder(yamlPath).sync().build()) {
            config.load();
            restored = deserializer.deserializeFields(config, TaskConfig::new);
        }

        // Verify values
        assertEquals("Build Feature X", restored.taskName.getValue());
        assertEquals(12345, restored.taskId.getValue());
        assertEquals(Priority.HIGH, restored.priority.getValue());
        assertEquals(3, restored.tags.size());
        assertEquals("backend", restored.tags.get(0).getValue());
        assertEquals("urgent", restored.tags.get(1).getValue());
        assertEquals("sprint-1", restored.tags.get(2).getValue());
        assertEquals(2, restored.previousPriorities.size());
        assertEquals(Priority.LOW, restored.previousPriorities.get(0).getValue());
        assertEquals(Priority.MEDIUM, restored.previousPriorities.get(1).getValue());
    }

    // ============ JSON Format Tests ============

    @Test
    public void testTypeAdapter_JsonFormat_SerializeAndDeserialize() throws IOException {
        Path jsonPath = tempDir.resolve("task-config.json");

        ObjectSerializer serializer = ObjectSerializer.builder()
                .withTypeAdapter(new BoxTypeAdapter<>())
                .build();
        ObjectDeserializer deserializer = ObjectDeserializer.builder()
                .withTypeAdapter(new BoxTypeAdapter<>())
                .build();

        TaskConfig original = new TaskConfig();
        original.taskName = new Box<>("JSON Test Task");
        original.taskId = new Box<>(99999);
        original.priority = new Box<>(Priority.CRITICAL);
        original.tags = List.of(new Box<>("json"), new Box<>("test"));
        original.previousPriorities = List.of(new Box<>(Priority.HIGH));

        // Serialize to JSON file
        try (FileConfig config = FileConfig.builder(jsonPath).sync().build()) {
            serializer.serializeFields(original, config);
            config.save();
        }

        // Verify file exists
        assertTrue(Files.exists(jsonPath), "JSON file should exist");
        String jsonContent = Files.readString(jsonPath);
        assertTrue(jsonContent.contains("JSON Test Task"), "JSON should contain task name");
        assertTrue(jsonContent.contains("99999"), "JSON should contain task ID");
        assertTrue(jsonContent.contains("CRITICAL"), "JSON should contain priority");

        // Deserialize from JSON file
        TaskConfig restored;
        try (FileConfig config = FileConfig.builder(jsonPath).sync().build()) {
            config.load();
            restored = deserializer.deserializeFields(config, TaskConfig::new);
        }

        assertEquals("JSON Test Task", restored.taskName.getValue());
        assertEquals(99999, restored.taskId.getValue());
        assertEquals(Priority.CRITICAL, restored.priority.getValue());
        assertEquals(2, restored.tags.size());
        assertEquals("json", restored.tags.get(0).getValue());
        assertEquals("test", restored.tags.get(1).getValue());
        assertEquals(1, restored.previousPriorities.size());
        assertEquals(Priority.HIGH, restored.previousPriorities.get(0).getValue());
    }

    // ============ TOML Format Tests ============

    @Test
    public void testTypeAdapter_TomlFormat_SerializeAndDeserialize() throws IOException {
        Path tomlPath = tempDir.resolve("task-config.toml");

        ObjectSerializer serializer = ObjectSerializer.builder()
                .withTypeAdapter(new BoxTypeAdapter<>())
                .build();
        ObjectDeserializer deserializer = ObjectDeserializer.builder()
                .withTypeAdapter(new BoxTypeAdapter<>())
                .build();

        TaskConfig original = new TaskConfig();
        original.taskName = new Box<>("TOML Configuration Task");
        original.taskId = new Box<>(77777);
        original.priority = new Box<>(Priority.LOW);
        original.tags = List.of(new Box<>("toml"), new Box<>("config"), new Box<>("test"));
        original.previousPriorities = List.of(new Box<>(Priority.MEDIUM), new Box<>(Priority.HIGH),
                new Box<>(Priority.LOW));

        // Serialize to TOML file
        try (CommentedFileConfig config = CommentedFileConfig.builder(tomlPath).sync().build()) {
            serializer.serializeFields(original, config);
            config.save();
        }

        // Verify file exists
        assertTrue(Files.exists(tomlPath), "TOML file should exist");
        String tomlContent = Files.readString(tomlPath);
        assertTrue(tomlContent.contains("TOML Configuration Task"), "TOML should contain task name");
        assertTrue(tomlContent.contains("77777"), "TOML should contain task ID");
        assertTrue(tomlContent.contains("LOW"), "TOML should contain priority");

        // Deserialize from TOML file
        TaskConfig restored;
        try (CommentedFileConfig config = CommentedFileConfig.builder(tomlPath).sync().build()) {
            config.load();
            restored = deserializer.deserializeFields(config, TaskConfig::new);
        }

        assertEquals("TOML Configuration Task", restored.taskName.getValue());
        assertEquals(77777, restored.taskId.getValue());
        assertEquals(Priority.LOW, restored.priority.getValue());
        assertEquals(3, restored.tags.size());
        assertEquals("toml", restored.tags.get(0).getValue());
        assertEquals("config", restored.tags.get(1).getValue());
        assertEquals("test", restored.tags.get(2).getValue());
        assertEquals(3, restored.previousPriorities.size());
        assertEquals(Priority.MEDIUM, restored.previousPriorities.get(0).getValue());
        assertEquals(Priority.HIGH, restored.previousPriorities.get(1).getValue());
        assertEquals(Priority.LOW, restored.previousPriorities.get(2).getValue());
    }

    // ============ Round-trip Tests Across Formats ============

    @Test
    public void testTypeAdapter_RoundTripAllFormats() throws IOException {
        ObjectSerializer serializer = ObjectSerializer.builder()
                .withTypeAdapter(new BoxTypeAdapter<>())
                .build();
        ObjectDeserializer deserializer = ObjectDeserializer.builder()
                .withTypeAdapter(new BoxTypeAdapter<>())
                .build();

        TaskConfig original = new TaskConfig();
        original.taskName = new Box<>("Cross-Format Test");
        original.taskId = new Box<>(11111);
        original.priority = new Box<>(Priority.MEDIUM);
        original.tags = List.of(new Box<>("cross"), new Box<>("format"));
        original.previousPriorities = List.of(new Box<>(Priority.LOW));

        // Test YAML
        Path yamlPath = tempDir.resolve("round-trip.yaml");
        try (CommentedFileConfig config = CommentedFileConfig.builder(yamlPath).sync().build()) {
            serializer.serializeFields(original, config);
            config.save();
            config.load();
            TaskConfig yamlRestored = deserializer.deserializeFields(config, TaskConfig::new);
            assertEquals("Cross-Format Test", yamlRestored.taskName.getValue(),
                    "YAML round-trip should preserve taskName");
            assertEquals(11111, yamlRestored.taskId.getValue(), "YAML round-trip should preserve taskId");
            assertEquals(Priority.MEDIUM, yamlRestored.priority.getValue(), "YAML round-trip should preserve priority");
        }

        // Test JSON
        Path jsonPath = tempDir.resolve("round-trip.json");
        try (FileConfig config = FileConfig.builder(jsonPath).sync().build()) {
            serializer.serializeFields(original, config);
            config.save();
            config.load();
            TaskConfig jsonRestored = deserializer.deserializeFields(config, TaskConfig::new);
            assertEquals("Cross-Format Test", jsonRestored.taskName.getValue(),
                    "JSON round-trip should preserve taskName");
            assertEquals(11111, jsonRestored.taskId.getValue(), "JSON round-trip should preserve taskId");
            assertEquals(Priority.MEDIUM, jsonRestored.priority.getValue(), "JSON round-trip should preserve priority");
        }

        // Test TOML
        Path tomlPath = tempDir.resolve("round-trip.toml");
        try (CommentedFileConfig config = CommentedFileConfig.builder(tomlPath).sync().build()) {
            serializer.serializeFields(original, config);
            config.save();
            config.load();
            TaskConfig tomlRestored = deserializer.deserializeFields(config, TaskConfig::new);
            assertEquals("Cross-Format Test", tomlRestored.taskName.getValue(),
                    "TOML round-trip should preserve taskName");
            assertEquals(11111, tomlRestored.taskId.getValue(), "TOML round-trip should preserve taskId");
            assertEquals(Priority.MEDIUM, tomlRestored.priority.getValue(), "TOML round-trip should preserve priority");
        }
    }

    @Test
    public void testTypeAdapter_InMemoryFormats() {
        ObjectSerializer serializer = ObjectSerializer.builder()
                .withTypeAdapter(new BoxTypeAdapter<>())
                .build();
        ObjectDeserializer deserializer = ObjectDeserializer.builder()
                .withTypeAdapter(new BoxTypeAdapter<>())
                .build();

        TaskConfig original = new TaskConfig();
        original.taskName = new Box<>("In-Memory Test");
        original.taskId = new Box<>(22222);
        original.priority = new Box<>(Priority.HIGH);
        original.tags = List.of(new Box<>("memory"));
        original.previousPriorities = List.of();

        // Test with YAML format in-memory
        Config yamlConfig = YamlFormat.defaultInstance().createConfig();
        serializer.serializeFields(original, yamlConfig);
        TaskConfig yamlRestored = deserializer.deserializeFields(yamlConfig, TaskConfig::new);
        assertEquals("In-Memory Test", yamlRestored.taskName.getValue());

        // Test with JSON format in-memory
        Config jsonConfig = JsonFormat.fancyInstance().createConfig();
        serializer.serializeFields(original, jsonConfig);
        TaskConfig jsonRestored = deserializer.deserializeFields(jsonConfig, TaskConfig::new);
        assertEquals("In-Memory Test", jsonRestored.taskName.getValue());

        // Test with TOML format in-memory
        Config tomlConfig = TomlFormat.instance().createConfig();
        serializer.serializeFields(original, tomlConfig);
        TaskConfig tomlRestored = deserializer.deserializeFields(tomlConfig, TaskConfig::new);
        assertEquals("In-Memory Test", tomlRestored.taskName.getValue());
    }
}
