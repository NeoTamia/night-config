package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * YAML format using SnakeYaml Engine V2 with comment support.
 *
 * @author TheElectronWill
 */
public final class YamlFormat implements ConfigFormat<CommentedConfig> {
    public static final LoadSettings LOAD_SETTINGS = LoadSettings.builder()
            .setAllowDuplicateKeys(false)
            .setMaxAliasesForCollections(50)
            .setAllowRecursiveKeys(false)
            .setParseComments(true)
            .build();
    public static final DumpSettings DUMP_SETTINGS = DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setIndent(2)
            .setCanonical(false)
            .setDumpComments(true)
            .setMultiLineFlow(true)
            .setIndicatorIndent(2)
            .setIndentWithIndicator(true)
            .build();
    private static final ThreadLocal<YamlFormat> LOCAL_DEFAULT_FORMAT = ThreadLocal.withInitial(
            () -> new YamlFormat(LOAD_SETTINGS, DUMP_SETTINGS));

    /**
     * @return the default instance of YamlFormat
     */
    public static YamlFormat defaultInstance() {
        return LOCAL_DEFAULT_FORMAT.get();
    }

    /**
     * Creates an instance of YamlFormat, set with the specified Load and Dump objects.
     *
     * @param loadSettings the LoadSettings object to use for parsing
     * @param dumpSettings the DumpSettings object to use for writing
     * @return a new instance of YamlFormat
     */
    public static YamlFormat configuredInstance(LoadSettings loadSettings, DumpSettings dumpSettings) {
        return new YamlFormat(loadSettings, dumpSettings);
    }

    /**
     * Creates an instance of YamlFormat, set with the specified Load object.
     *
     * @param loadSettings the LoadSettings object to use
     * @return a new instance of YamlFormat
     */
    public static YamlFormat configuredInstance(LoadSettings loadSettings) {
        return new YamlFormat(loadSettings, DUMP_SETTINGS);
    }

    /**
     * @return a new config with the format {@link YamlFormat#defaultInstance()}.
     */
    public static Config newConfig() {
        return defaultInstance().createConfig();
    }

    /**
     * @return a new config with the given map creator
     */
    public static Config newConfig(Supplier<Map<String, Object>> mapCreator) {
        return defaultInstance().createConfig(mapCreator);
    }

    /**
     * @return a new concurrent config with the format {@link YamlFormat#defaultInstance()}.
     */
    public static Config newConcurrentConfig() {
        return defaultInstance().createConcurrentConfig();
    }

    static {
        FormatDetector.registerExtension("yaml", YamlFormat::defaultInstance);
        FormatDetector.registerExtension("yml", YamlFormat::defaultInstance);
    }

    private final LoadSettings loadSettings;
    private final Load yaml;
    private final DumpSettings dumpSettings;
    private final Dump yamlDump;

    private YamlFormat(LoadSettings loadSettings, DumpSettings dumpSettings) {
        this.yaml = new Load(loadSettings);
        this.yamlDump = new Dump(dumpSettings);
        this.loadSettings = loadSettings;
        this.dumpSettings = dumpSettings;
    }

    @Override
    public ConfigWriter createWriter() {
        return new YamlWriter(yamlDump);
    }

    @Override
    public ConfigParser<CommentedConfig> createParser() {
        return new YamlParser(this);
    }

    @Override
    public CommentedConfig createConfig(Supplier<Map<String, Object>> mapCreator) {
        return CommentedConfig.of(mapCreator, this);
    }

    @Override
    public boolean supportsComments() {
        return true;
    }

    @Override
    public boolean supportsType(Class<?> type) {
        return type == null
                || type.isEnum()
                || type == Boolean.class
                || type == String.class
                || type == java.util.Date.class
                || type == java.sql.Date.class
                || type == java.sql.Timestamp.class
                || type == byte[].class
                || type == Object[].class
                || Number.class.isAssignableFrom(type)
                || Set.class.isAssignableFrom(type)
                || List.class.isAssignableFrom(type)
                || Config.class.isAssignableFrom(type);
    }

    public LoadSettings getLoadSettings() {
        return loadSettings;
    }

    public Load getYaml() {
        return yaml;
    }

    public DumpSettings getDumpSettings() {
        return dumpSettings;
    }

    public Dump getYamlDump() {
        return yamlDump;
    }
}
