package re.neotamia.nightconfig.hocon;

import com.typesafe.config.*;
import re.neotamia.nightconfig.core.CommentedConfig;
import re.neotamia.nightconfig.core.Config;
import re.neotamia.nightconfig.core.ConfigFormat;
import re.neotamia.nightconfig.core.concurrent.ConcurrentCommentedConfig;
import re.neotamia.nightconfig.core.io.ConfigParser;
import re.neotamia.nightconfig.core.io.ParsingException;
import re.neotamia.nightconfig.core.io.ParsingMode;

import java.io.Reader;
import java.util.*;

import static re.neotamia.nightconfig.core.NullObject.NULL_OBJECT;

/**
 * A HOCON parser that uses the typesafehub config library.
 *
 * @author TheElectronWill
 * @see <a href="https://github.com/typesafehub/config/blob/master/HOCON.md">HOCON spec by
 * typesafehub</a>
 */
public final class HoconParser implements ConfigParser<CommentedConfig> {
    private static final ConfigParseOptions OPTIONS = ConfigParseOptions.defaults()
            .setAllowMissing(false)
            .setSyntax(ConfigSyntax.CONF);

    @Override
    public ConfigFormat<CommentedConfig> getFormat() {
        return HoconFormat.instance();
    }

    @Override
    public CommentedConfig parse(Reader reader) {
        CommentedConfig config = HoconFormat.instance().createConfig();
        parse(reader, config, ParsingMode.MERGE);
        return config;
    }

    @Override
    public void parse(Reader reader, Config destination, ParsingMode parsingMode) {
        try {
            ConfigObject parsed = ConfigFactory.parseReader(reader, OPTIONS).resolve().root();

            if (destination instanceof ConcurrentCommentedConfig conf) {
                conf.bulkCommentedUpdate(view -> {
                    parsingMode.prepareParsing(view);
                    put(parsed, view, parsingMode);
                });
            } else if (destination instanceof CommentedConfig commentedConfig) {
                parsingMode.prepareParsing(destination);
                put(parsed, commentedConfig, parsingMode);
                parseHeaderComment(reader, commentedConfig);
            } else {
                parsingMode.prepareParsing(destination);
                put(parsed, destination, parsingMode);
            }
        } catch (Exception e) {
            throw new ParsingException("HOCON parsing failed", e);
        }
    }

    private static void put(ConfigObject typesafeConfig, Config destination, ParsingMode parsingMode) {
        for (Map.Entry<String, ConfigValue> entry : typesafeConfig.entrySet()) {
            List<String> path = ConfigUtil.splitPath(entry.getKey());
            parsingMode.put(destination, path, unwrap(entry.getValue().unwrapped()));
        }
    }

    private static void put(ConfigObject typesafeConfig, CommentedConfig destination, ParsingMode parsingMode) {
        for (Map.Entry<String, ConfigValue> entry : typesafeConfig.entrySet()) {
            List<String> path = Collections.singletonList(entry.getKey());
            ConfigValue value = entry.getValue();
            if (value instanceof ConfigObject) {
                CommentedConfig subConfig = destination.createSubConfig();
                put((ConfigObject) value, subConfig, parsingMode);
                parsingMode.put(destination, path, subConfig);
            } else {
                parsingMode.put(destination, path, unwrap(value.unwrapped()));
            }
            List<String> comments = value.origin().comments();
            if (!comments.isEmpty())
                destination.setComment(path, String.join("\n", value.origin().comments()).replace("\r", "").stripLeading());
        }
    }

    @SuppressWarnings("unchecked")
    private static Object unwrap(Object o) {
        if (o instanceof Map) {
            Map<String, ?> map = (Map<String, ?>) o;
            Map<String, Object> unwrappedMap = new HashMap<>(map.size());
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                unwrappedMap.put(entry.getKey(), unwrap(entry.getValue()));
            }
            return Config.wrap(unwrappedMap, HoconFormat.instance());
        } else if (o instanceof List<?> list) {
            if (!list.isEmpty() && list.getFirst() instanceof Map) {
                List<Config> configList = new ArrayList<>();
                for (Object element : list) {
                    configList.add((Config) unwrap(element));
                }
                return configList;
            }
        } else if (o == null) {
            return NULL_OBJECT;
        }
        return o;
    }
}
