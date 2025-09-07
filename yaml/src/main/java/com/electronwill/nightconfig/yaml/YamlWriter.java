package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.concurrent.StampedConfig;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.WritingException;
import com.electronwill.nightconfig.core.utils.TransformingList;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.StreamDataWriter;
import org.snakeyaml.engine.v2.comments.CommentLine;
import org.snakeyaml.engine.v2.comments.CommentType;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.*;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;
import static com.electronwill.nightconfig.yaml.YamlFormat.DUMP_SETTINGS;

/**
 * A YAML writer that uses the SnakeYaml Engine V2 library.
 *
 * @author TheElectronWill
 */
public final class YamlWriter implements ConfigWriter {
    private final Dump yaml;

    public YamlWriter() {
        this(new Dump(DUMP_SETTINGS));
    }

    public YamlWriter(Dump yaml) {
        this.yaml = yaml;
    }

    public YamlWriter(DumpSettings settings) {
        this(new Dump(settings));
    }

    @Override
    public void write(UnmodifiableConfig config, Writer writer) {
        if (config instanceof StampedConfig stampedConfig) {
            // StampedConfig does not support valueMap(), use the accumulator
            config = stampedConfig.newAccumulatorCopy();
        }
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
        if (config instanceof UnmodifiableCommentedConfig commentedConfig) {
            // create yaml nodes with comments
            try {
                Node rootNode = createNodeWithComments(commentedConfig);
                yaml.dumpNode(rootNode, streamWriter);
                return; // Don't fall through to regular dump
            } catch (Exception e) {
                throw new WritingException("YAML writing with comments failed", e);
            }
        }

        try {
            Map<String, Object> unwrappedMap = unwrap(config);
            // Use StreamDataWriter to wrap the Writer for SnakeYaml Engine V2
            yaml.dump(unwrappedMap, streamWriter);
        } catch (Exception e) {
            throw new WritingException("YAML writing failed", e);
        }
    }

    /**
     * Creates a YAML node with comments from an UnmodifiableCommentedConfig.
     */
    private static Node createNodeWithComments(UnmodifiableCommentedConfig config) {
        List<NodeTuple> tuples = new ArrayList<>();

        for (UnmodifiableCommentedConfig.Entry entry : config.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String comment = entry.getComment();

            // Create key node with comment
            Node keyNode = new ScalarNode(Tag.STR, key, ScalarStyle.PLAIN);

            // Attach comment to key node if present
            if (comment != null && !comment.trim().isEmpty()) {
                List<CommentLine> blockComments = new ArrayList<>();
                // Split multi-line comments and add each line
                String[] commentLines = comment.split("\n");
                for (String commentLine : commentLines) {
                    String trimmedLine = commentLine.trim();
                    // Remove leading # if present, SnakeYAML will add it automatically with proper spacing
                    if (trimmedLine.startsWith("#"))
                        trimmedLine = trimmedLine.substring(1).trim();
                    // Pass comment with leading space for proper formatting
                    blockComments.add(new CommentLine(Optional.empty(), Optional.empty(), " " + trimmedLine, CommentType.BLOCK));
                }
                keyNode.setBlockComments(blockComments);
            }

            // Create value node
            Node valueNode = createValueNode(value);

            tuples.add(new NodeTuple(keyNode, valueNode));
        }

        return new MappingNode(Tag.MAP, tuples, FlowStyle.BLOCK);
    }

    /**
     * Creates a YAML node for a value, handling different types appropriately.
     */
    private static Node createValueNode(Object value) {
        if (value == null || value == NULL_OBJECT) {
            return new ScalarNode(Tag.NULL, "null", ScalarStyle.PLAIN);
        } else if (value instanceof UnmodifiableCommentedConfig commentedConfig) {
            return createNodeWithComments(commentedConfig);
        } else if (value instanceof UnmodifiableConfig config) {
            return createNodeWithComments(UnmodifiableCommentedConfig.fake(config));
        } else if (value instanceof List<?> list) {
            List<Node> nodes = new ArrayList<>();
            for (Object item : list) {
                nodes.add(createValueNode(item));
            }
            return new SequenceNode(Tag.SEQ, nodes, FlowStyle.BLOCK);
        } else if (value instanceof Enum<?> enumValue) {
            return new ScalarNode(Tag.STR, enumValue.name(), ScalarStyle.PLAIN);
        }
        return new ScalarNode(Tag.STR, String.valueOf(value), ScalarStyle.PLAIN);
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
