import re.neotamia.nightconfig.core.Config;
import re.neotamia.nightconfig.core.serde.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Example showing TypeAdapter with multiple generic type parameters like
 * {@code Pair<K, V>}.
 * <p>
 * This demonstrates how to handle types with more than one type parameter,
 * extracting and using each one during serialization and deserialization.
 */
public class TypeAdapterMultipleTypeParamsExample {

    public static void main(String[] args) {
        System.out.println("=== TypeAdapter with Multiple Type Parameters ===\n");

        ObjectSerializer serializer = ObjectSerializer.builder()
                .withTypeAdapter(new PairTypeAdapter<>())
                .build();

        ObjectDeserializer deserializer = ObjectDeserializer.builder()
                .withTypeAdapter(new PairTypeAdapter<>())
                .build();

        // Create config with Pair fields
        GameConfig config = new GameConfig();
        config.playerPosition = new Pair<>(100, 250);
        config.screenSize = new Pair<>(1920, 1080);
        config.playerName = new Pair<>("Player1", 9001);
        config.settings = List.of(
                new Pair<>("volume", 75),
                new Pair<>("brightness", 100),
                new Pair<>("difficulty", 3));

        System.out.println("Original config:");
        System.out.println("  playerPosition: " + config.playerPosition);
        System.out.println("  screenSize: " + config.screenSize);
        System.out.println("  playerName: " + config.playerName);
        System.out.println("  settings: " + config.settings);

        // Serialize
        Config serialized = serializer.serializeFields(config, Config::inMemory);
        System.out.println("\nSerialized: " + serialized);

        // Deserialize
        GameConfig restored = deserializer.deserializeFields(serialized, GameConfig::new);

        System.out.println("\nRestored config:");
        System.out.println("  playerPosition: " + restored.playerPosition);
        System.out.println("  screenSize: " + restored.screenSize);
        System.out.println("  playerName: " + restored.playerName);
        System.out.println("  settings: " + restored.settings);

        // Verify
        System.out.println("\nVerification:");
        System.out.println("  Position match: " +
                (config.playerPosition.first().equals(restored.playerPosition.first()) &&
                        config.playerPosition.second().equals(restored.playerPosition.second())));
        System.out.println("  Settings count match: " +
                (config.settings.size() == restored.settings.size()));
    }

    // ============ Pair Class with Two Type Parameters ============

    static class Pair<K, V> {
        private K first;
        private V second;

        public Pair() {
        }

        public Pair(K first, V second) {
            this.first = first;
            this.second = second;
        }

        public K first() {
            return first;
        }

        public V second() {
            return second;
        }

        @Override
        public String toString() {
            return "(" + first + ", " + second + ")";
        }
    }

    static class GameConfig {
        public Pair<Integer, Integer> playerPosition = new Pair<>(0, 0);
        public Pair<Integer, Integer> screenSize = new Pair<>(800, 600);
        public Pair<String, Integer> playerName = new Pair<>("Unknown", 0);
        public List<Pair<String, Integer>> settings = List.of();

        public GameConfig() {
        }
    }

    // ============ TypeAdapter for Pair<K, V> ============

    /**
     * TypeAdapter that serializes Pair<K, V> as a map with "first" and "second"
     * keys.
     */
    static class PairTypeAdapter<K, V> implements TypeAdapter<Pair<K, V>, Object> {

        @Override
        public boolean canHandle(Type type) {
            if (type instanceof ParameterizedType pt) {
                return pt.getRawType() == Pair.class;
            }
            return type == Pair.class;
        }

        @Override
        public Object serialize(Pair<K, V> value, Type type, SerializerContext ctx) {
            // Serialize as a map with "first" and "second" keys
            Config map = ctx.createConfig();
            map.set("first", ctx.serializeValue(value.first()));
            map.set("second", ctx.serializeValue(value.second()));
            return map;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Pair<K, V> deserialize(Object value, Type type, DeserializerContext ctx) {
            // Extract both type parameters
            Type firstType = Object.class;
            Type secondType = Object.class;

            if (type instanceof ParameterizedType pt) {
                Type[] typeArgs = pt.getActualTypeArguments();
                if (typeArgs.length >= 1)
                    firstType = typeArgs[0];
                if (typeArgs.length >= 2)
                    secondType = typeArgs[1];
            }

            // Value should be a map/config
            if (value instanceof Map<?, ?> map) {
                K first = (K) ctx.deserializeValue(map.get("first"), new TypeConstraint(firstType));
                V second = (V) ctx.deserializeValue(map.get("second"), new TypeConstraint(secondType));
                return new Pair<>(first, second);
            }

            throw new SerdeException("Expected a map for Pair deserialization, got: " + value.getClass());
        }
    }
}
