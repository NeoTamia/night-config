import com.electronwill.nightconfig.core.concurrent.SynchronizedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.serde.ObjectSerializer;
import com.electronwill.nightconfig.core.serde.annotations.SerdeComment;
import com.electronwill.nightconfig.core.serde.annotations.SerdeKey;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializerTest {
    @Test
    public void serializeYaml() throws IOException {
        var path = Path.of("src/test/resources/config-night.yaml");
        var config = CommentedFileConfig.builder(path).sync().build();
        ObjectSerializer.standard().serializeFields(new Config(), config);
        config.save();

        var configState = Files.readString(path);
        config.load();

        assertEquals("Config", config.get("name"));
        assertEquals(2, config.getInt("version"));
        assertEquals(true, config.get("isEnabled"));
        assertEquals("Whether the configuration is enabled", config.getComment("isEnabled"));
        assertEquals(0.5f, config.getFloat("decimals"));
        assertEquals(0.123456789, config.get("doubleValue"));
        assertEquals(List.of("item1", "item2", "item3"), config.get("items"));
        var settings = (SynchronizedConfig) config.get("settings");
        assertEquals(Map.of("key1", "value1", "key2", "value2"), settings.valueMap());
        assertEquals("Nested Config", config.get("nested.description"));
        assertEquals(10, config.getInt("nested.count"));
        assertEquals(false, config.get("nested.innerConfig.flag"));
        assertEquals(3.14, config.get("nested.innerConfig.ratio"));
        assertEquals("""
                This is a multiline
                string example.
                It preserves line breaks.""", config.get("multiline"));
        assertEquals("OWO", config.get("test"));
        assertEquals("example", config.get("resource.namespace"));
        assertEquals("resource_path", config.get("resource.path"));
        assertEquals(true, config.get("uneVariableOuLeNomPeutEtreTresLong"));
        assertEquals(42, config.getInt("toto"));
        assertEquals(24, config.getInt("titi"));

        config.save();
        var configState2 = Files.readString(path);
        assertEquals(configState, configState2);
    }

    @Test
    public void serializeToml() throws IOException {
        var path = Path.of("src/test/resources/config-night.toml");
        var config = CommentedFileConfig.builder(path).sync().build();
        ObjectSerializer.standard().serializeFields(new Config(), config);
        config.save();

        var configState = Files.readString(path);
        config.load();

        assertEquals("Config", config.get("name"));
        assertEquals(2, config.getInt("version"));
        assertEquals(true, config.get("isEnabled"));
        assertEquals("Whether the configuration is enabled", config.getComment("isEnabled"));
        assertEquals(0.5f, config.getFloat("decimals"));
        assertEquals(0.123456789, config.get("doubleValue"));
        assertEquals(List.of("item1", "item2", "item3"), config.get("items"));
        var settings = (SynchronizedConfig) config.get("settings");
        assertEquals(Map.of("key1", "value1", "key2", "value2"), settings.valueMap());
        assertEquals("Nested Config", config.get("nested.description"));
        assertEquals(10, config.getInt("nested.count"));
        assertEquals(false, config.get("nested.innerConfig.flag"));
        assertEquals(3.14, config.get("nested.innerConfig.ratio"));
        assertEquals("""
                This is a multiline
                string example.
                It preserves line breaks.""", config.get("multiline").toString().replace("\r", ""));
        assertEquals("OWO", config.get("test"));
        assertEquals("example", config.get("resource.namespace"));
        assertEquals("resource_path", config.get("resource.path"));
        assertEquals(true, config.get("uneVariableOuLeNomPeutEtreTresLong"));
        assertEquals(42, config.getInt("toto"));
        assertEquals(24, config.getInt("titi"));

        config.set("multiline", config.get("multiline").toString().replace("\r", ""));
        config.save();
        var configState2 = Files.readString(path);
        assertEquals(configState, configState2);
    }

    @Test
    public void serializeJson() throws IOException {
        var path = Path.of("src/test/resources/config-night.json");
        var config = FileConfig.builder(path).sync().build();
        ObjectSerializer.standard().serializeFields(new Config(), config);
        config.save();

        var configState = Files.readString(path);
        config.load();

        assertEquals("Config", config.get("name"));
        assertEquals(2, config.getInt("version"));
        assertEquals(true, config.get("isEnabled"));
        assertEquals(0.5f, config.getFloat("decimals"));
        assertEquals(0.123456789, config.get("doubleValue"));
        assertEquals(List.of("item1", "item2", "item3"), config.get("items"));
        var settings = (SynchronizedConfig) config.get("settings");
        assertEquals(Map.of("key1", "value1", "key2", "value2"), settings.valueMap());
        assertEquals("Nested Config", config.get("nested.description"));
        assertEquals(10, config.getInt("nested.count"));
        assertEquals(false, config.get("nested.innerConfig.flag"));
        assertEquals(3.14, config.get("nested.innerConfig.ratio"));
        assertEquals("""
                This is a multiline
                string example.
                It preserves line breaks.""", config.get("multiline"));
        assertEquals("OWO", config.get("test"));
        assertEquals("example", config.get("resource.namespace"));
        assertEquals("resource_path", config.get("resource.path"));
        assertEquals(true, config.get("uneVariableOuLeNomPeutEtreTresLong"));
        assertEquals(42, config.getInt("toto"));
        assertEquals(24, config.getInt("titi"));

        config.save();
        var configState2 = Files.readString(path);
        assertEquals(configState, configState2);    }

    public static class Config {
        private final String name = "Config";
        private final int version = 2;
        @SerdeKey("isEnabled")
        @SerdeComment("Whether the configuration is enabled")
        private final boolean enabled = true;
        private final float decimals = 0.5f;
        private final double doubleValue = 0.123456789;
        private final List<String> items = List.of("item1", "item2", "item3");
        private final Map<String, String> settings = Map.of("key1", "value1", "key2", "value2");
        private final NestedConfig nested = new NestedConfig();
        private final String multiline =
                """
                        This is a multiline
                        string example.
                        It preserves line breaks.
                        """.trim();
        private final EnumTest test = EnumTest.OWO;
        private final ResourceLocation resource = new ResourceLocation("example:resource_path");
        private final boolean uneVariableOuLeNomPeutEtreTresLong = true;
        private final int toto = 42;
        private final int titi = 24;
    }

    enum EnumTest {
        UWU,
        OWO
    }

    public record ResourceLocation(String namespace, String path) {
        public ResourceLocation(String location) {
            this(location.contains(":") ? location.split(":", 2)[0] : "minecraft",
                    location.contains(":") ? location.split(":", 2)[1] : location);
        }
    }

    public static class NestedConfig {
        private final String description = "Nested Config";
        private final int count = 10;
        private final InnerConfig innerConfig = new InnerConfig();
    }

    public static class InnerConfig {
        private final boolean flag = false;
        private final double ratio = 3.14;
    }

}
