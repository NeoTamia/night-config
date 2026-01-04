import re.neotamia.nightconfig.core.Config;
import re.neotamia.nightconfig.core.serde.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Advanced example showing TypeAdapter with nested generic types like
 * {@code List<Box<T>>}.
 * <p>
 * This demonstrates how TypeAdapter properly handles complex nested generics
 * where you have collections containing generic wrapper types.
 */
public class TypeAdapterWithListExample {

    public static void main(String[] args) {
        System.out.println("=== TypeAdapter with List Example ===\n");

        // Create serializer and deserializer
        ObjectSerializer serializer = ObjectSerializer.builder()
                .withTypeAdapter(new BoxTypeAdapter<>())
                .build();

        ObjectDeserializer deserializer = ObjectDeserializer.builder()
                .withTypeAdapter(new BoxTypeAdapter<>())
                .build();

        // Create config with List<Box<T>> fields
        TaskConfig config = new TaskConfig();
        config.taskName = new Box<>("Build Feature");
        config.priority = new Box<>(Priority.HIGH);
        config.tags = List.of(
                new Box<>("backend"),
                new Box<>("urgent"),
                new Box<>("sprint-1"));
        config.priorityHistory = List.of(
                new Box<>(Priority.LOW),
                new Box<>(Priority.MEDIUM),
                new Box<>(Priority.HIGH));

        System.out.println("Original config:");
        System.out.println("  taskName: " + config.taskName);
        System.out.println("  priority: " + config.priority);
        System.out.println("  tags: " + config.tags);
        System.out.println("  priorityHistory: " + config.priorityHistory);

        // Serialize
        Config serialized = serializer.serializeFields(config, Config::inMemory);
        System.out.println("\nSerialized: " + serialized);

        // Deserialize
        TaskConfig restored = deserializer.deserializeFields(serialized, TaskConfig::new);

        System.out.println("\nRestored config:");
        System.out.println("  taskName: " + restored.taskName);
        System.out.println("  priority: " + restored.priority);
        System.out.println("  tags: " + restored.tags);
        System.out.println("  priorityHistory: " + restored.priorityHistory);

        // Verify List<Box<String>>
        System.out.println("\nVerifying tags:");
        for (int i = 0; i < config.tags.size(); i++) {
            String original = config.tags.get(i).getValue();
            String restored_ = restored.tags.get(i).getValue();
            System.out.println("  [" + i + "] " + original + " == " + restored_ + " : " + original.equals(restored_));
        }

        // Verify List<Box<Priority>>
        System.out.println("\nVerifying priorityHistory:");
        for (int i = 0; i < config.priorityHistory.size(); i++) {
            Priority original = config.priorityHistory.get(i).getValue();
            Priority restored_ = restored.priorityHistory.get(i).getValue();
            System.out.println("  [" + i + "] " + original + " == " + restored_ + " : " + (original == restored_));
        }
    }

    // ============ Data Classes ============

    enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

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
            return "Box{" + value + '}';
        }
    }

    static class TaskConfig {
        public Box<String> taskName = new Box<>("");
        public Box<Priority> priority = new Box<>(Priority.LOW);
        public List<Box<String>> tags = List.of();
        public List<Box<Priority>> priorityHistory = List.of();

        public TaskConfig() {
        }
    }

    // ============ TypeAdapter ============

    static class BoxTypeAdapter<T> implements TypeAdapter<Box<T>, Object> {

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
            Type valueType = Object.class;
            if (type instanceof ParameterizedType pt) {
                Type[] typeArgs = pt.getActualTypeArguments();
                if (typeArgs.length > 0) {
                    valueType = typeArgs[0];
                }
            }
            T innerValue = (T) ctx.deserializeValue(value, new TypeConstraint(valueType));
            return new Box<>(innerValue);
        }
    }
}
