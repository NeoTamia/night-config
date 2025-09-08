package re.neotamia.nightconfig.core.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import re.neotamia.nightconfig.core.CommentedConfig;
import re.neotamia.nightconfig.core.Config;
import re.neotamia.nightconfig.core.ConfigFormat;
import re.neotamia.nightconfig.core.InMemoryCommentedFormat;
import re.neotamia.nightconfig.core.UnmodifiableConfig;
import re.neotamia.nightconfig.core.io.ConfigParser;
import re.neotamia.nightconfig.core.io.ConfigWriter;
import re.neotamia.nightconfig.core.io.ParsingMode;
import re.neotamia.nightconfig.core.io.WritingException;

final class Util {
    /** Test load() with ParsingMode.REPLACE. The entire config should be replaced. */
    static void testLoadReplace(FileConfig config) {
        config.set("a", "old A");
        config.set("c", "old C");
        config.set("nested.c", "nested.c");
        config.set("nested.sub", 1234);
		config.set("array", "123"); // not an array, but it will be replaced so it should work
        config.load();
        assertEquals("new A", config.get("a"));
        assertEquals("new B", config.get("b"));
        assertFalse(config.contains("c"));
        assertEquals("nested.a", config.get("nested.a"));
        assertEquals("nested.b", config.get("nested.b"));
        assertNotEquals(1234, config.<Object>get("nested.sub"));
        assertEquals("nss", config.get("nested.sub.sub"));
		assertInstanceOf(List.class, config.get("array"));
		assertInstanceOf(Config.class, (config.<List<Object>>get("array")).get(0));
		assertEquals("value", ((Config)(config.<List<Object>>get("array")).get(0)).get("inConfigInArray"));
    }

    /**
     * Test load() with ParsingMode.ADD. The existing values should be kept as is, and some new values should be
     * added.
     */
    static void testLoadAdd(FileConfig config) {
        config.set("a", "old A");
        config.set("c", "old C");
        config.set("nested.c", "nested.c");
        config.set("nested.sub", 1234);
        config.load();
        // check old values
        assertEquals("old A", config.get("a"));
        assertEquals("old C", config.get("c"));
        assertEquals("nested.c", config.get("nested.c"));
        assertEquals(1234, config.<Integer>get("nested.sub"));
        // check new values (shallow)
        assertEquals("new B", config.get("b"));
        assertFalse(config.contains("nested.a"));
        assertFalse(config.contains("nested.b"));
        assertFalse(config.contains("nested.sub.sub"));
		assertEquals("value", ((Config)(config.<List<Object>>get("array")).get(0)).get("inConfigInArray"));
    }

    /**
     * Test load() with ParsingMode.MERGE. The existing values should be replaced if they have a "newer" value.
     */
    static void testLoadMerge(FileConfig config) {
        config.set("a", "old A");
        config.set("c", "old C");
        config.set("nested.c", "nested.c");
        config.set("nested.sub", 1234);
        config.load();
        assertEquals("new A", config.get("a"));
        assertEquals("new B", config.get("b"));
        assertEquals("old C", config.get("c"));
        assertEquals("nested.a", config.get("nested.a"));
        assertEquals("nested.b", config.get("nested.b"));
        assertFalse(config.contains("nested.c"));
        assertEquals("nss", config.get("nested.sub.sub"));
        assertNotEquals(1234, config.<Object>get("nested.sub"));
		assertEquals("value", ((Config)(config.<List<Object>>get("array")).get(0)).get("inConfigInArray"));
    }

    static final class TestFormat implements ConfigFormat<CommentedConfig> {
        volatile long writeTime = -1;
		final boolean useProperSubConfigType;

        public TestFormat(boolean useProperSubConfigType) {
			this.useProperSubConfigType = useProperSubConfigType;
		}

		@Override
        public CommentedConfig createConfig(Supplier<Map<String, Object>> mapCreator) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ConfigParser<CommentedConfig> createParser() {
            return new TestParser(useProperSubConfigType);
        }

        @Override
        public ConfigWriter createWriter() {
            return new TestWriter(this);
        }

        @Override
        public boolean supportsComments() {
            return true;
        }
    }

    static final class TestWriter implements ConfigWriter {
        private final TestFormat format;

        TestWriter(TestFormat format) {
            this.format = format;
        }

        @Override
        public void write(UnmodifiableConfig config, Writer writer) {
            format.writeTime = System.currentTimeMillis();
            for (UnmodifiableConfig.Entry e : config.entrySet()) {
                try {
                    writer.write(e.getKey() + " = " + e.getValue());
                    writer.flush();
                } catch (IOException ex) {
                    throw new WritingException(ex);
                }
            }
        }
    }

    static final class TestParser implements ConfigParser<CommentedConfig> {
		private final boolean useProperSubConfigType;

        public TestParser(boolean useProperSubConfigType) {
			this.useProperSubConfigType = useProperSubConfigType;
		}

		@Override
        public ConfigFormat<CommentedConfig> getFormat() {
            return InMemoryCommentedFormat.defaultInstance();
        }

        @Override
        public CommentedConfig parse(Reader reader) {
            CommentedConfig config = CommentedConfig.inMemory();
            parseInto(config, ParsingMode.REPLACE);
            return config;
        }

        @Override
        public void parse(Reader reader, Config config, ParsingMode parsingMode) {
            parseInto(config, parsingMode);
        }

        @Override
        public void parse(Path file, Config destination, ParsingMode parsingMode,
                FileNotFoundAction nefAction, Charset charset) {
            parseInto(destination, parsingMode);
        }

		private Config newSubConfig(Config config) {
			return useProperSubConfigType ? config.createSubConfig() : Config.inMemory();
		}

        private void parseInto(Config config, ParsingMode parsingMode) {
            parsingMode.prepareParsing(config);
            parsingMode.put(config, "a", "new A");
            parsingMode.put(config, "b", "new B");

			var sub = newSubConfig(config);
            var subsub = newSubConfig(config);
            sub.set("a", "nested.a");
            sub.set("b", "nested.b");
            sub.set("sub", subsub);
            subsub.set("sub", "nss");
            parsingMode.put(config, "nested", sub);

			var inArray = newSubConfig(config);
			inArray.set("inConfigInArray", "value");
			parsingMode.put(config, "array", List.of(inArray));
        }
    }
}
