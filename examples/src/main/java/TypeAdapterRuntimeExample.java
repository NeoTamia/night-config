import re.neotamia.nightconfig.core.Config;
import re.neotamia.nightconfig.core.serde.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Example showing runtime registration of TypeAdapter using
 * {@code registerTypeAdapter()}.
 * <p>
 * This approach is useful when you need to dynamically add TypeAdapters at
 * runtime,
 * rather than at builder time.
 */
public class TypeAdapterRuntimeExample {

    public static void main(String[] args) {
        System.out.println("=== TypeAdapter Runtime Registration Example ===\n");

        // Create standard serializer and deserializer
        ObjectSerializer serializer = ObjectSerializer.standard();
        ObjectDeserializer deserializer = ObjectDeserializer.standard();

        // Register TypeAdapter at runtime (after creation)
        serializer.registerTypeAdapter(new OptionalValueAdapter<>());
        deserializer.registerTypeAdapter(new OptionalValueAdapter<>());

        System.out.println("TypeAdapter registered at runtime!\n");

        // Create config with OptionalValue fields
        UserPreferences prefs = new UserPreferences();
        prefs.theme = new OptionalValue<>("dark");
        prefs.fontSize = new OptionalValue<>(14);
        prefs.language = new OptionalValue<>(); // empty/null

        System.out.println("Original prefs:");
        System.out.println("  theme: " + prefs.theme);
        System.out.println("  fontSize: " + prefs.fontSize);
        System.out.println("  language: " + prefs.language);

        // Serialize
        Config serialized = serializer.serializeFields(prefs, Config::inMemory);
        System.out.println("\nSerialized: " + serialized);

        // Deserialize
        UserPreferences restored = deserializer.deserializeFields(serialized, UserPreferences::new);

        System.out.println("\nRestored prefs:");
        System.out.println("  theme: " + restored.theme);
        System.out.println("  fontSize: " + restored.fontSize);
        System.out.println("  language: " + restored.language);

        // Demonstrate dynamic adapter registration
        System.out.println("\n--- Dynamic Registration Demo ---");
        ObjectSerializer dynamicSerializer = ObjectSerializer.standard();

        // Before registration - would serialize as nested object
        System.out.println("Before TypeAdapter registration:");
        System.out.println("  Box fields would be serialized as nested objects");

        // Register adapter
        dynamicSerializer.registerTypeAdapter(new OptionalValueAdapter<>());
        System.out.println("\nAfter TypeAdapter registration:");
        System.out.println("  Box fields are now serialized to their inner values!");

        Config result = dynamicSerializer.serializeFields(prefs, Config::inMemory);
        System.out.println("  Result: " + result);
    }

    // ============ OptionalValue - Similar to Optional but serializable
    // ============

    static class OptionalValue<T> {
        private T value;

        public OptionalValue() {
            this.value = null;
        }

        public OptionalValue(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public boolean isPresent() {
            return value != null;
        }

        @Override
        public String toString() {
            return isPresent() ? "OptionalValue[" + value + "]" : "OptionalValue.empty";
        }
    }

    static class UserPreferences {
        public OptionalValue<String> theme = new OptionalValue<>();
        public OptionalValue<Integer> fontSize = new OptionalValue<>();
        public OptionalValue<String> language = new OptionalValue<>();

        public UserPreferences() {
        }
    }

    // ============ TypeAdapter for OptionalValue<T> ============

    static class OptionalValueAdapter<T> implements TypeAdapter<OptionalValue<T>, Object> {

        @Override
        public boolean canHandle(Type type) {
            if (type instanceof ParameterizedType pt) {
                return pt.getRawType() == OptionalValue.class;
            }
            return type == OptionalValue.class;
        }

        @Override
        public Object serialize(OptionalValue<T> value, Type type, SerializerContext ctx) {
            // Return null for empty, otherwise serialize the inner value
            if (!value.isPresent()) {
                return null;
            }
            return ctx.serializeValue(value.getValue());
        }

        @Override
        @SuppressWarnings("unchecked")
        public OptionalValue<T> deserialize(Object value, Type type, DeserializerContext ctx) {
            if (value == null) {
                return new OptionalValue<>();
            }

            Type valueType = Object.class;
            if (type instanceof ParameterizedType pt) {
                Type[] typeArgs = pt.getActualTypeArguments();
                if (typeArgs.length > 0) {
                    valueType = typeArgs[0];
                }
            }

            T innerValue = (T) ctx.deserializeValue(value, new TypeConstraint(valueType));
            return new OptionalValue<>(innerValue);
        }
    }
}
