import re.neotamia.nightconfig.core.Config;
import re.neotamia.nightconfig.core.serde.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Advanced example demonstrating TypeAdapter for complex nested generic types.
 * <p>
 * This example shows how to handle a complex wrapper class that contains:
 * - A List of Box&lt;T&gt; elements
 * - Metadata about the collection
 * <p>
 * The key challenge is properly handling the nested generics when
 * serializing and deserializing the container type.
 */
public class TypeAdapterComplexNestedExample {

    public static void main(String[] args) {
        System.out.println("=== Complex Nested Generic TypeAdapter Example ===\n");

        // Create serializer and deserializer with BOTH adapters
        // Order matters: more specific adapters first!
        ObjectSerializer serializer = ObjectSerializer.builder()
                .withTypeAdapter(new BoxCollectionAdapter<>()) // Handle BoxCollection<T>
                .withTypeAdapter(new BoxAdapter<>()) // Handle Box<T>
                .build();

        ObjectDeserializer deserializer = ObjectDeserializer.builder()
                .withTypeAdapter(new BoxCollectionAdapter<>())
                .withTypeAdapter(new BoxAdapter<>())
                .build();

        // ========== Example 1: BoxCollection<String> ==========
        System.out.println("--- Example 1: BoxCollection<String> ---");

        StringCollectionConfig stringConfig = new StringCollectionConfig();
        stringConfig.names = new BoxCollection<>("User Names");
        stringConfig.names.add(new Box<>("Alice"));
        stringConfig.names.add(new Box<>("Bob"));
        stringConfig.names.add(new Box<>("Charlie"));

        System.out.println("Original: " + stringConfig.names);

        Config serialized = serializer.serializeFields(stringConfig, Config::inMemory);
        System.out.println("Serialized: " + serialized);

        StringCollectionConfig restored = deserializer.deserializeFields(serialized, StringCollectionConfig::new);
        System.out.println("Restored: " + restored.names);

        // ========== Example 2: BoxCollection<Integer> ==========
        System.out.println("\n--- Example 2: BoxCollection<Integer> ---");

        IntegerCollectionConfig intConfig = new IntegerCollectionConfig();
        intConfig.scores = new BoxCollection<>("High Scores");
        intConfig.scores.add(new Box<>(1000));
        intConfig.scores.add(new Box<>(850));
        intConfig.scores.add(new Box<>(720));

        System.out.println("Original: " + intConfig.scores);

        Config serializedInt = serializer.serializeFields(intConfig, Config::inMemory);
        System.out.println("Serialized: " + serializedInt);

        IntegerCollectionConfig restoredInt = deserializer.deserializeFields(serializedInt,
                IntegerCollectionConfig::new);
        System.out.println("Restored: " + restoredInt.scores);

        // ========== Example 3: BoxCollection<Priority> (Enum) ==========
        System.out.println("\n--- Example 3: BoxCollection<Priority> ---");

        PriorityCollectionConfig priorityConfig = new PriorityCollectionConfig();
        priorityConfig.taskPriorities = new BoxCollection<>("Task Priorities");
        priorityConfig.taskPriorities.add(new Box<>(Priority.HIGH));
        priorityConfig.taskPriorities.add(new Box<>(Priority.MEDIUM));
        priorityConfig.taskPriorities.add(new Box<>(Priority.LOW));

        System.out.println("Original: " + priorityConfig.taskPriorities);

        Config serializedPriority = serializer.serializeFields(priorityConfig, Config::inMemory);
        System.out.println("Serialized: " + serializedPriority);

        PriorityCollectionConfig restoredPriority = deserializer.deserializeFields(serializedPriority,
                PriorityCollectionConfig::new);
        System.out.println("Restored: " + restoredPriority.taskPriorities);

        // ========== Verification ==========
        System.out.println("\n--- Verification ---");
        System.out.println("String collection size match: " +
                (stringConfig.names.items.size() == restored.names.items.size()));
        System.out.println("Int collection size match: " +
                (intConfig.scores.items.size() == restoredInt.scores.items.size()));
        System.out.println("Priority collection size match: " +
                (priorityConfig.taskPriorities.items.size() == restoredPriority.taskPriorities.items.size()));
    }

    // ============ Data Classes ============

    enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    /**
     * Simple generic box.
     */
    static class Box<T> {
        private T value;

        public Box() {
        }

        public Box(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Box[" + value + "]";
        }
    }

    /**
     * Complex container that holds a List of Box&lt;T&gt; plus metadata.
     * This is the challenging type to serialize/deserialize.
     */
    static class BoxCollection<T> {
        private String label;
        private List<Box<T>> items = new ArrayList<>();

        public BoxCollection() {
            this.label = "";
        }

        public BoxCollection(String label) {
            this.label = label;
        }

        public void add(Box<T> box) {
            items.add(box);
        }

        public String getLabel() {
            return label;
        }

        public List<Box<T>> getItems() {
            return items;
        }

        @Override
        public String toString() {
            return "BoxCollection{label='" + label + "', items=" + items + "}";
        }
    }

    // Config classes for different Box types
    static class StringCollectionConfig {
        public BoxCollection<String> names = new BoxCollection<>();

        public StringCollectionConfig() {
        }
    }

    static class IntegerCollectionConfig {
        public BoxCollection<Integer> scores = new BoxCollection<>();

        public IntegerCollectionConfig() {
        }
    }

    static class PriorityCollectionConfig {
        public BoxCollection<Priority> taskPriorities = new BoxCollection<>();

        public PriorityCollectionConfig() {
        }
    }

    // ============ TypeAdapters ============

    /**
     * Simple TypeAdapter for Box&lt;T&gt;.
     */
    static class BoxAdapter<T> implements TypeAdapter<Box<T>, Object> {

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
            Type innerType = Object.class;
            if (type instanceof ParameterizedType pt) {
                Type[] args = pt.getActualTypeArguments();
                if (args.length > 0)
                    innerType = args[0];
            }
            T inner = (T) ctx.deserializeValue(value, new TypeConstraint(innerType));
            return new Box<>(inner);
        }
    }

    /**
     * Complex TypeAdapter for BoxCollection&lt;T&gt;.
     * <p>
     * This demonstrates how to:
     * 1. Serialize a complex object with nested generics
     * 2. Extract type parameters from the outer type to use for inner types
     * 3. Properly reconstruct the nested generic structure on deserialization
     */
    static class BoxCollectionAdapter<T> implements TypeAdapter<BoxCollection<T>, Object> {

        @Override
        public boolean canHandle(Type type) {
            if (type instanceof ParameterizedType pt) {
                return pt.getRawType() == BoxCollection.class;
            }
            return type == BoxCollection.class;
        }

        @Override
        public Object serialize(BoxCollection<T> value, Type type, SerializerContext ctx) {
            // Serialize as a config with "label" and "items" fields
            Config config = ctx.createConfig();
            config.set("label", value.getLabel());

            // Serialize each Box<T> in the list
            List<Object> serializedItems = new ArrayList<>();
            for (Box<T> box : value.getItems()) {
                // Serialize the Box's inner value directly
                serializedItems.add(ctx.serializeValue(box.getValue()));
            }
            config.set("items", serializedItems);

            return config;
        }

        @Override
        @SuppressWarnings("unchecked")
        public BoxCollection<T> deserialize(Object value, Type type, DeserializerContext ctx) {
            // Extract T from BoxCollection<T>
            Type elementType = Object.class;
            if (type instanceof ParameterizedType pt) {
                Type[] args = pt.getActualTypeArguments();
                if (args.length > 0) {
                    elementType = args[0];
                }
            }

            // Value should be a Map/Config
            if (!(value instanceof Map<?, ?> map)) {
                throw new SerdeException("Expected map for BoxCollection, got: " + value.getClass());
            }

            // Extract label
            String label = (String) map.get("label");
            BoxCollection<T> result = new BoxCollection<>(label);

            // Extract and deserialize items
            Object itemsObj = map.get("items");
            if (itemsObj instanceof List<?> itemsList) {
                for (Object item : itemsList) {
                    // Deserialize each item as type T, then wrap in Box
                    T deserializedValue = (T) ctx.deserializeValue(item, new TypeConstraint(elementType));
                    result.add(new Box<>(deserializedValue));
                }
            }

            return result;
        }
    }
}
