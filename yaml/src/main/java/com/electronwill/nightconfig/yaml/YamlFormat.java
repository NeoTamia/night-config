package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.FormatDetector;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.function.Supplier;

/**
 * YAML format using SnakeYaml Engine V2 with comment support.
 *
 * @author TheElectronWill
 */
public final class YamlFormat implements ConfigFormat<Config> {
	private static final ThreadLocal<YamlFormat> LOCAL_DEFAULT_FORMAT = ThreadLocal.withInitial(
		() -> new YamlFormat(createDefaultLoad(), createDefaultDump()));

	private static Load createDefaultLoad() {
		LoadSettings settings = LoadSettings.builder()
			.setAllowDuplicateKeys(false)
			.setMaxAliasesForCollections(50)
			.setAllowRecursiveKeys(false)
			.setParseComments(true)
			.build();
		return new Load(settings);
	}

	private static Dump createDefaultDump() {
		DumpSettings settings = DumpSettings.builder()
			.setDefaultFlowStyle(FlowStyle.BLOCK)
			.setIndent(2)
			.setCanonical(false)
			.setDumpComments(true)
			.setMultiLineFlow(true)
			.build();
		return new Dump(settings);
	}

	/**
	 * @return the default instance of YamlFormat
	 */
	public static YamlFormat defaultInstance() {
		return LOCAL_DEFAULT_FORMAT.get();
	}

	/**
	 * Creates an instance of YamlFormat, set with the specified Load and Dump objects.
	 *
	 * @param load the Load object to use for parsing
	 * @param dump the Dump object to use for writing
	 * @return a new instance of YamlFormat
	 */
	public static YamlFormat configuredInstance(Load load, Dump dump) {
		return new YamlFormat(load, dump);
	}

	/**
	 * Creates an instance of YamlFormat, set with the specified Load object.
	 *
	 * @param load the Load object to use
	 * @return a new instance of YamlFormat
	 */
	public static YamlFormat configuredInstance(Load load) {
		return new YamlFormat(load, createDefaultDump());
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

	final Load yaml;
	final Dump yamlDump;

	private YamlFormat(Load yaml, Dump yamlDump) {
		this.yaml = yaml;
		this.yamlDump = yamlDump;
	}

	@Override
	public ConfigWriter createWriter() {
		return new YamlWriter(yamlDump);
	}

	@Override
	public ConfigParser<Config> createParser() {
		return new YamlParser(this);
	}

	@Override
	public Config createConfig(Supplier<Map<String, Object>> mapCreator) {
		return Config.of(mapCreator, this);
	}

	@Override
	public boolean supportsComments() {
		return true; // SnakeYaml Engine V2 supports comments
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
}
