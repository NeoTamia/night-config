import re.neotamia.nightconfig.core.Config;
import re.neotamia.nightconfig.core.serde.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Example showing how to use {@link TypeAdapter} for custom generic type
 * handling.
 * <p>
 * TypeAdapter allows you to serialize and deserialize generic types like
 * {@code Box<T>}
 * while preserving the full generic type information using Java's reflection
 * API.
 */
public class TypeAdapterExample {

    public static void main(String[] args) {
        System.out.println("=== TypeAdapter Example ===\n");

        // Create serializer and deserializer with our custom TypeAdapter
        ObjectSerializer serializer = ObjectSerializer.builder()
                .withTypeAdapter(new BoxTypeAdapter<>())
                .build();

        ObjectDeserializer deserializer = ObjectDeserializer.builder()
                .withTypeAdapter(new BoxTypeAdapter<>())
                .build();

        // Create a config object with Box fields
        MyConfig config = new MyConfig();
        config.name = new Box<>("Hello NightConfig!");
        config.count = new Box<>(42);
        config.enabled = new Box<>(true);

        System.out.println("Original object:");
        System.out.println("  name: " + config.name);
        System.out.println("  count: " + config.count);
        System.out.println("  enabled: " + config.enabled);

        // Serialize to Config
        Config serialized = serializer.serializeFields(config, Config::inMemory);
        System.out.println("\nSerialized config: " + serialized);

        // Notice that Box<T> values are serialized to their inner values!
        // This is because our TypeAdapter extracts the inner value during
        // serialization.

        // Deserialize back to object
        MyConfig restored = deserializer.deserializeFields(serialized, MyConfig::new);

        System.out.println("\nRestored object:");
        System.out.println("  name: " + restored.name);
        System.out.println("  count: " + restored.count);
        System.out.println("  enabled: " + restored.enabled);

        // Verify the round-trip
        System.out.println("\nRound-trip verification:");
        System.out.println("  name equals: " + config.name.getValue().equals(restored.name.getValue()));
        System.out.println("  count equals: " + config.count.getValue().equals(restored.count.getValue()));
        System.out.println("  enabled equals: " + config.enabled.getValue().equals(restored.enabled.getValue()));
    }

    // ============ Generic Box Class ============

    /**
     * A simple generic container class.
     * This is similar to Optional but for demonstration purposes.
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

        public void setValue(T value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Box{" + value + '}';
        }
    }

    // ============ Config Class with Box Fields ============

    static class MyConfig {
        public Box<String> name = new Box<>("default");
        public Box<Integer> count = new Box<>(0);
        public Box<Boolean> enabled = new Box<>(false);

        public MyConfig() {
        }
    }

    // ============ TypeAdapter for Box<T> ============

    /**
     * A TypeAdapter that handles serialization and deserialization of
     * {@code Box<T>}.
     * <p>
     * Key features:
     * <ul>
     * <li>{@code canHandle(Type)} checks if the type is Box or Box<T></li>
     * <li>{@code serialize} extracts the inner value from Box</li>
     * <li>{@code deserialize} uses the generic type info to properly deserialize
     * the inner value</li>
     * </ul>
     */
    static class BoxTypeAdapter<T> implements TypeAdapter<Box<T>, Object> {

        @Override
        public boolean canHandle(Type type) {
            // Check if it's a parameterized Box<T> or raw Box class
            if (type instanceof ParameterizedType pt) {
                return pt.getRawType() == Box.class;
            }
            return type == Box.class;
        }

        @Override
        public Object serialize(Box<T> value, Type type, SerializerContext ctx) {
            // Serialize the inner value, not the Box itself
            return ctx.serializeValue(value.getValue());
        }

        @Override
        @SuppressWarnings("unchecked")
        public Box<T> deserialize(Object value, Type type, DeserializerContext ctx) {
            // Extract the type argument T from Box<T>
            Type valueType = Object.class;
            if (type instanceof ParameterizedType pt) {
                Type[] typeArgs = pt.getActualTypeArguments();
                if (typeArgs.length > 0) {
                    valueType = typeArgs[0];
                }
            }

            // Deserialize using the extracted type
            T innerValue = (T) ctx.deserializeValue(value, new TypeConstraint(valueType));
            return new Box<>(innerValue);
        }
    }
}
