package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.concurrent.StampedConfig;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.WritingException;
import com.electronwill.nightconfig.core.utils.TransformingList;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.StreamDataWriter;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;

/**
 * A YAML writer that uses the SnakeYaml Engine V2 library.
 *
 * @author TheElectronWill
 */
public final class YamlWriter implements ConfigWriter {
    private final Dump yaml;

    public YamlWriter() {
        this(new Dump(DumpSettings.builder().build()));
    }

    public YamlWriter(Dump yaml) {
        this.yaml = yaml;
    }

    public YamlWriter(DumpSettings settings) {
        this(new Dump(settings));
    }

    @Override
    public void write(UnmodifiableConfig config, Writer writer) {
        if (config instanceof StampedConfig) {
            // StampedConfig does not support valueMap(), use the accumulator
            config = ((StampedConfig) config).newAccumulatorCopy();
        }
        try {
            Map<String, Object> unwrappedMap = unwrap(config);
            // Use StreamDataWriter to wrap the Writer for SnakeYaml Engine V2
            StreamDataWriter streamWriter = new StreamDataWriter() {
                @Override
                public void write(String str) {
                    try {
                        writer.write(str);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

				@Override
				public void write(String str, int off, int len) {
					try {
						writer.write(str, off, len);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}

                @Override
                public void flush() {
                    try {
                        writer.flush();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            yaml.dump(unwrappedMap, streamWriter);
        } catch (Exception e) {
            throw new WritingException("YAML writing failed", e);
        }
    }

    private static Map<String, Object> unwrap(UnmodifiableConfig config) {
        return new TransformingMap<>(config.valueMap(), YamlWriter::unwrapObject, v -> v, v -> v);
    }

    private static List<Object> unwrapList(List<Object> list) {
        return new TransformingList<>(list, YamlWriter::unwrapObject, v -> v, v -> v);
    }

    @SuppressWarnings("unchecked")
    private static Object unwrapObject(Object value) {
        if (value instanceof UnmodifiableConfig unmodifiableConfig)
            return unwrap(unmodifiableConfig);
        if (value instanceof List)
            return unwrapList((List<Object>) value);
        if (value == NULL_OBJECT)
            return null;
        // Convert enums to their string representation to avoid SnakeYAML Engine tags
        if (value instanceof Enum)
            return ((Enum<?>) value).name();
        return value;
    }
}
