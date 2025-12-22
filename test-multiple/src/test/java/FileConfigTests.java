import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import re.neotamia.nightconfig.core.CommentedConfig;
import re.neotamia.nightconfig.core.Config;
import re.neotamia.nightconfig.core.file.CommentedFileConfig;
import re.neotamia.nightconfig.core.file.FileConfig;
import re.neotamia.nightconfig.core.file.FormatDetector;
import re.neotamia.nightconfig.json.JsonFormat;
import re.neotamia.nightconfig.toml.TomlFormat;
import re.neotamia.nightconfig.yaml.YamlFormat;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileConfigTests {

    @BeforeAll
    public static void registerFormats() {
        FormatDetector.registerExtension("json", JsonFormat.fancyInstance());
        FormatDetector.registerExtension("toml", TomlFormat.instance());
        FormatDetector.registerExtension("yaml", YamlFormat.defaultInstance());
        FormatDetector.registerExtension("yml", YamlFormat.defaultInstance());
    }

    @Test
    public void asyncJson() throws URISyntaxException {
        Path jsonResource = Path.of(getClass().getResource("/simple_config.json").toURI());
        FileConfig fileConfig = FileConfig.builder(jsonResource).async().build();
        fileConfig.load();
        fileConfig.close();

        assertEquals(2024, fileConfig.getInt(List.of("year")));
        assertInstanceOf(List.class, fileConfig.get(List.of("dependencies", "testproject")));
        assertEquals("dep-a", (fileConfig.<List<Config>>get(List.of("dependencies", "testproject"))).getFirst().get("a"));
        assertNull((fileConfig.<List<Config>>get(List.of("dependencies", "testproject"))).getFirst().get("null"));
        assertTrue((fileConfig.<List<Config>>get(List.of("dependencies", "testproject"))).getFirst().contains("null"));
    }

    @Test
    public void asyncToml() throws URISyntaxException {
        Path tomlResource = Path.of(getClass().getResource("/simple_config.toml").toURI());
        FileConfig fileConfig = FileConfig.builder(tomlResource).async().build();
        fileConfig.load();
        fileConfig.close();

        assertEquals(2024, fileConfig.getInt(List.of("year")));
        assertInstanceOf(List.class, fileConfig.get(List.of("dependencies", "testproject")));
        assertEquals("dep-a", (fileConfig.<List<Config>>get(List.of("dependencies", "testproject"))).getFirst().get("a"));
    }

    @Test
    public void asyncForgeTest() throws URISyntaxException {
        Path forgeTestResource = Path.of(getClass().getResource("/forge_test.toml").toURI());
        FileConfig fileConfig = FileConfig.builder(forgeTestResource).async().build();
        fileConfig.load(); // initial load
        System.out.println("loaded: " + fileConfig);

        checkForgeTestContent(fileConfig);
        fileConfig.load(); // reload (should have the same content)
        checkForgeTestContent(fileConfig);
        fileConfig.close(); // close (should not modify the config)
        checkForgeTestContent(fileConfig);
    }

    @Test
    public void syncForgeTest() throws URISyntaxException {
        Path forgeTestResource = Path.of(getClass().getResource("/forge_test.toml").toURI());
        FileConfig fileConfig = FileConfig.builder(forgeTestResource).sync().build();
        fileConfig.load(); // initial load
        System.out.println("loaded: " + fileConfig);

        checkForgeTestContent(fileConfig);
        fileConfig.load(); // reload (should have the same content)
        checkForgeTestContent(fileConfig);
        fileConfig.close(); // close (should not modify the config)
        checkForgeTestContent(fileConfig);
    }

    @Test
    public void asyncMiniTest() throws Exception {
        Path forgeTestResource = Path.of(getClass().getResource("/mini_test.toml").toURI());
        FileConfig fileConfig = FileConfig.builder(forgeTestResource).async().build();
        fileConfig.load();
        assertEquals(1, fileConfig.getInt("v"));

        List<Config> arrayOfTables = fileConfig.get(List.of("a.b", "b.c"));
        assertEquals("value", arrayOfTables.getFirst().get("key"));
    }

    @Test
    public void syncMiniTest() throws Exception {
        Path forgeTestResource = Path.of(getClass().getResource("/mini_test.toml").toURI());
        FileConfig fileConfig = FileConfig.builder(forgeTestResource).sync().build();
        fileConfig.load();
        assertEquals(1, fileConfig.getInt("v"));

        List<Config> arrayOfTables = fileConfig.get(List.of("a.b", "b.c"));
        assertEquals("value", arrayOfTables.getFirst().get("key"));
    }

    void checkForgeTestContent(FileConfig fileConfig) {
        assertEquals("javafml", fileConfig.get("modLoader"));
        assertEquals("[3,)", fileConfig.get("loaderVersion"));
        assertEquals("CC0", fileConfig.get("license"));

        List<Config> mods = fileConfig.get("mods");
        Config mod0 = mods.getFirst();
        assertEquals("testproject", mod0.get("modId"));
        assertEquals("A test project.", mod0.get("description"));

        List<Config> dependencies = fileConfig.get("dependencies.testproject");
        Config dep0 = dependencies.getFirst();
        assertEquals("neoforge", dep0.get("modId"));
        assertEquals("required", dep0.get("type"));
        assertEquals("NONE", dep0.get("ordering"));
        assertEquals("BOTH", dep0.get("side"));
    }

    @Test
    public void asyncTomlCommentsTest() throws Exception {
        Path forgeTestResource = Path.of(getClass().getResource("/comments_test.toml").toURI());
        CommentedFileConfig fileConfig = CommentedFileConfig.builder(forgeTestResource).async().build();
        fileConfig.load(); // initial load
        System.out.println("loaded: " + fileConfig);

        checkCommentsTestContent(fileConfig);
        fileConfig.load(); // reload (should have the same content)
        checkCommentsTestContent(fileConfig);
        fileConfig.close(); // close (should not modify the config)
        checkCommentsTestContent(fileConfig);
    }

    @Test
    public void syncTomlCommentsTest() throws Exception {
        Path forgeTestResource = Path.of(getClass().getResource("/comments_test.toml").toURI());
        CommentedFileConfig fileConfig = CommentedFileConfig.builder(forgeTestResource).sync().build();
        fileConfig.load(); // initial load
        System.out.println("loaded: " + fileConfig);

        checkCommentsTestContent(fileConfig);
        fileConfig.load(); // reload (should have the same content)
        checkCommentsTestContent(fileConfig);
        fileConfig.close(); // close (should not modify the config)
        checkCommentsTestContent(fileConfig);
    }

    void checkCommentsTestContent(CommentedFileConfig config) {
        assertEquals(1, config.getInt("v"));
        assertEquals("comment on v", config.getComment("v"));

        List<CommentedConfig> abbc = config.get(List.of("a.b", "b.c"));
        assertNotNull(abbc);
        assertEquals("value", abbc.getFirst().get("key"));
        assertEquals("sub1", abbc.getFirst().getComment("key"));

        assertEquals("comment on table", config.getComment("table"));
        assertEquals("v", config.get(List.of("table", "k")));
        assertEquals("sub2", config.getComment(List.of("table", "k")));

        assertEquals("comment on subtable", config.getComment(List.of("table", "subtable")));
        assertEquals("b", config.get(List.of("table", "subtable", "a")));
        assertEquals("sub3", config.getComment(List.of("table", "subtable", "a")));
    }
}
