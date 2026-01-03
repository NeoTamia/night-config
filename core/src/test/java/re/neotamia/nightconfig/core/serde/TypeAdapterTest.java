package re.neotamia.nightconfig.core.serde;

import org.junit.jupiter.api.Test;
import re.neotamia.nightconfig.core.Config;
import re.neotamia.nightconfig.core.serde.annotations.SerdeDefault;
import re.neotamia.nightconfig.core.serde.annotations.SerdePhase;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link TypeAdapter} interface to verify proper generic type
 * handling
 * with {@link Type} and {@link ParameterizedType}.
 */
public class TypeAdapterTest {

    // ============ Test Data Classes ============

    /**
     * A simple generic container class for testing.
     */
    public static class Box<T> {
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
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Box<?> other))
                return false;
            return value != null ? value.equals(other.value) : other.value == null;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Box{" + value + '}';
        }
    }

    /**
     * A class with generic Box fields for testing type resolution.
     */
    public static class ContainerWithBox {
        public Box<String> stringBox;
        public Box<Integer> integerBox;
        public List<Box<String>> boxList;
    }

    /**
     * A POJO class with Box fields for integration testing.
     */
    public static class MyConfig {
        public Box<String> name = new Box<>("default");
        public Box<Integer> count = new Box<>(0);

        public MyConfig() {
        }
    }

    // ============ TypeAdapter Implementation for Box<T> ============

    /**
     * A TypeAdapter that handles Box<T> serialization and deserialization.
     */
    public static class BoxTypeAdapter implements TypeAdapter<Box<?>, Object> {

        @Override
        public boolean canHandle(Type type) {
            if (type instanceof ParameterizedType pt) {
                return pt.getRawType() == Box.class;
            }
            return type == Box.class;
        }

        @Override
        public Object serialize(Box<?> value, Type type, SerializerContext ctx) {
            // For Box, we serialize the inner value
            return ctx.serializeValue(value.getValue());
        }

        @Override
        public Box<?> deserialize(Object value, Type type, DeserializerContext ctx) {
            // Extract the type argument T from Box<T>
            Type valueType = Object.class;
            if (type instanceof ParameterizedType pt) {
                Type[] typeArgs = pt.getActualTypeArguments();
                if (typeArgs.length > 0) {
                    valueType = typeArgs[0];
                }
            }

            // Deserialize the inner value using the extracted type
            Object innerValue = ctx.deserializeValue(value, new TypeConstraint(valueType));
            return new Box<>(innerValue);
        }
    }

    // ============ Integration Tests with ObjectSerializer/ObjectDeserializer
    // ============

    @Test
    public void testRegisterTypeAdapter_SerializeFieldsWithBoxString() {
        // Create serializer with BoxTypeAdapter registered
        ObjectSerializer serializer = ObjectSerializer.standard();
        serializer.registerTypeAdapter(new BoxTypeAdapter());

        // Create config object with Box fields
        MyConfig config = new MyConfig();
        config.name = new Box<>("Hello World");
        config.count = new Box<>(42);

        // Serialize to Config
        Config result = serializer.serializeFields(config, Config::inMemory);

        // Verify Box values were serialized to their inner values
        assertEquals("Hello World", result.get("name"), "Box<String> should serialize to its inner value");
        assertEquals(42, result.getInt("count"), "Box<Integer> should serialize to its inner value");
    }

    @Test
    public void testRegisterTypeAdapter_DeserializeFieldsWithBoxString() {
        // Create deserializer with BoxTypeAdapter registered
        ObjectDeserializer deserializer = ObjectDeserializer.standard();
        deserializer.registerTypeAdapter(new BoxTypeAdapter());

        // Create config with values
        Config config = Config.inMemory();
        config.set("name", "Deserialized Name");
        config.set("count", 123);

        // Deserialize to MyConfig
        MyConfig result = deserializer.deserializeFields(config, MyConfig::new);

        // Verify values were deserialized into Box instances
        assertNotNull(result.name, "name should not be null");
        assertNotNull(result.count, "count should not be null");
        assertEquals("Deserialized Name", result.name.getValue(), "Box<String> should contain deserialized value");
        assertEquals(123, result.count.getValue(), "Box<Integer> should contain deserialized value");
    }

    @Test
    public void testWithTypeAdapter_BuilderPattern() {
        // Test builder pattern for serializer
        ObjectSerializer serializer = ObjectSerializer.builder()
                .withTypeAdapter(new BoxTypeAdapter())
                .build();

        // Test builder pattern for deserializer
        ObjectDeserializer deserializer = ObjectDeserializer.builder()
                .withTypeAdapter(new BoxTypeAdapter())
                .build();

        // Verify they work
        MyConfig original = new MyConfig();
        original.name = new Box<>("Builder Test");
        original.count = new Box<>(999);

        Config serialized = serializer.serializeFields(original, Config::inMemory);
        assertEquals("Builder Test", serialized.get("name"));
        assertEquals(999, serialized.getInt("count"));

        MyConfig deserialized = deserializer.deserializeFields(serialized, MyConfig::new);
        assertEquals("Builder Test", deserialized.name.getValue());
        assertEquals(999, deserialized.count.getValue());
    }

    // ============ Tests for List<Box<T>> ============

    /**
     * A POJO class with List<Box<T>> fields for testing nested generic types.
     */
    public static class ConfigWithListOfBoxes {
        public List<Box<String>> names = List.of();
        public List<Box<Integer>> counts = List.of();

        public ConfigWithListOfBoxes() {
        }
    }

    @Test
    public void testRegisterTypeAdapter_SerializeListOfBoxString() {
        ObjectSerializer serializer = ObjectSerializer.standard();
        serializer.registerTypeAdapter(new BoxTypeAdapter());

        ConfigWithListOfBoxes config = new ConfigWithListOfBoxes();
        config.names = List.of(new Box<>("Alice"), new Box<>("Bob"), new Box<>("Charlie"));
        config.counts = List.of(new Box<>(10), new Box<>(20), new Box<>(30));

        Config result = serializer.serializeFields(config, Config::inMemory);

        // List<Box<T>> should serialize to List of inner values
        List<?> serializedNames = result.get("names");
        assertNotNull(serializedNames, "names should not be null");
        assertEquals(3, serializedNames.size(), "Should have 3 names");
        assertEquals("Alice", serializedNames.get(0), "First name should be Alice");
        assertEquals("Bob", serializedNames.get(1), "Second name should be Bob");
        assertEquals("Charlie", serializedNames.get(2), "Third name should be Charlie");

        List<?> serializedCounts = result.get("counts");
        assertNotNull(serializedCounts, "counts should not be null");
        assertEquals(3, serializedCounts.size(), "Should have 3 counts");
        assertEquals(10, serializedCounts.get(0), "First count should be 10");
        assertEquals(20, serializedCounts.get(1), "Second count should be 20");
        assertEquals(30, serializedCounts.get(2), "Third count should be 30");
    }

    @Test
    public void testRegisterTypeAdapter_DeserializeListOfBoxString() {
        ObjectDeserializer deserializer = ObjectDeserializer.standard();
        deserializer.registerTypeAdapter(new BoxTypeAdapter());

        Config config = Config.inMemory();
        config.set("names", List.of("Alice", "Bob", "Charlie"));
        config.set("counts", List.of(10, 20, 30));

        ConfigWithListOfBoxes result = deserializer.deserializeFields(config, ConfigWithListOfBoxes::new);

        // Values should be deserialized into List<Box<T>>
        assertNotNull(result.names, "names should not be null");
        assertEquals(3, result.names.size(), "Should have 3 names");
        assertEquals("Alice", result.names.get(0).getValue(), "First box should contain Alice");
        assertEquals("Bob", result.names.get(1).getValue(), "Second box should contain Bob");
        assertEquals("Charlie", result.names.get(2).getValue(), "Third box should contain Charlie");

        assertNotNull(result.counts, "counts should not be null");
        assertEquals(3, result.counts.size(), "Should have 3 counts");
        assertEquals(10, result.counts.get(0).getValue(), "First box should contain 10");
        assertEquals(20, result.counts.get(1).getValue(), "Second box should contain 20");
        assertEquals(30, result.counts.get(2).getValue(), "Third box should contain 30");
    }

    @Test
    public void testWithTypeAdapter_SerializeListOfBoxes() {
        ObjectSerializer serializer = ObjectSerializer.builder()
                .withTypeAdapter(new BoxTypeAdapter())
                .build();

        ConfigWithListOfBoxes config = new ConfigWithListOfBoxes();
        config.names = List.of(new Box<>("X"), new Box<>("Y"));

        Config result = serializer.serializeFields(config, Config::inMemory);

        List<?> serializedNames = result.get("names");
        assertNotNull(serializedNames);
        assertEquals(2, serializedNames.size());
        assertEquals("X", serializedNames.get(0));
        assertEquals("Y", serializedNames.get(1));
    }

    @Test
    public void testWithTypeAdapter_DeserializeListOfBoxes() {
        ObjectDeserializer deserializer = ObjectDeserializer.builder()
                .withTypeAdapter(new BoxTypeAdapter())
                .build();

        Config config = Config.inMemory();
        config.set("names", List.of("A", "B"));
        config.set("counts", List.of(100, 200));

        ConfigWithListOfBoxes result = deserializer.deserializeFields(config, ConfigWithListOfBoxes::new);

        assertEquals(2, result.names.size());
        assertEquals("A", result.names.get(0).getValue());
        assertEquals("B", result.names.get(1).getValue());
        assertEquals(2, result.counts.size());
        assertEquals(100, result.counts.get(0).getValue());
        assertEquals(200, result.counts.get(1).getValue());
    }

    @Test
    public void testRoundTrip_SerializeAndDeserializeListOfBoxes() {
        // Create serializer and deserializer with BoxTypeAdapter
        ObjectSerializer serializer = ObjectSerializer.builder()
                .withTypeAdapter(new BoxTypeAdapter())
                .build();
        ObjectDeserializer deserializer = ObjectDeserializer.builder()
                .withTypeAdapter(new BoxTypeAdapter())
                .build();

        // Create original config
        ConfigWithListOfBoxes original = new ConfigWithListOfBoxes();
        original.names = List.of(new Box<>("Round"), new Box<>("Trip"), new Box<>("Test"));
        original.counts = List.of(new Box<>(1), new Box<>(2), new Box<>(3));

        // Serialize to Config
        Config serialized = serializer.serializeFields(original, Config::inMemory);

        // Deserialize back to object
        ConfigWithListOfBoxes restored = deserializer.deserializeFields(serialized, ConfigWithListOfBoxes::new);

        // Verify round-trip preserves values
        assertEquals(3, restored.names.size());
        assertEquals("Round", restored.names.get(0).getValue());
        assertEquals("Trip", restored.names.get(1).getValue());
        assertEquals("Test", restored.names.get(2).getValue());

        assertEquals(3, restored.counts.size());
        assertEquals(1, restored.counts.get(0).getValue());
        assertEquals(2, restored.counts.get(1).getValue());
        assertEquals(3, restored.counts.get(2).getValue());
    }

    @Test
    public void testRoundTrip_SerializeAndDeserializeSimpleBoxes() {
        ObjectSerializer serializer = ObjectSerializer.standard();
        serializer.registerTypeAdapter(new BoxTypeAdapter());

        ObjectDeserializer deserializer = ObjectDeserializer.standard();
        deserializer.registerTypeAdapter(new BoxTypeAdapter());

        // Original
        MyConfig original = new MyConfig();
        original.name = new Box<>("RoundTrip");
        original.count = new Box<>(42);

        // Serialize
        Config serialized = serializer.serializeFields(original, Config::inMemory);
        assertEquals("RoundTrip", serialized.get("name"));
        assertEquals(42, serialized.getInt("count"));

        // Deserialize
        MyConfig restored = deserializer.deserializeFields(serialized, MyConfig::new);
        assertEquals("RoundTrip", restored.name.getValue());
        assertEquals(42, restored.count.getValue());
    }

    // ============ Tests for Enum with Generic BoxTypeAdapter<T> ============

    /**
     * An enum for testing generic TypeAdapter with enum types.
     */
    public enum Status {
        PENDING,
        ACTIVE,
        COMPLETED,
        CANCELLED
    }

    /**
     * A POJO class with List<Box<Status>> for testing generic TypeAdapter with
     * enums.
     */
    public static class ConfigWithEnumBoxes {
        public Box<Status> currentStatus = new Box<>(Status.PENDING);
        public List<Box<Status>> statusHistory = List.of();

        public ConfigWithEnumBoxes() {
        }
    }

    /**
     * A generic TypeAdapter that handles Box<T> for a specific type T.
     * This allows for type-specific registration like
     * {@code new GenericBoxTypeAdapter<Status>()}.
     *
     * @param <T> the type of value inside the Box
     */
    public static class GenericBoxTypeAdapter<T> implements TypeAdapter<Box<T>, Object> {

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

    @Test
    public void testGenericBoxTypeAdapter_SerializeEnumBox() {
        ObjectSerializer serializer = ObjectSerializer.standard();
        serializer.registerTypeAdapter(new GenericBoxTypeAdapter<Status>());

        ConfigWithEnumBoxes config = new ConfigWithEnumBoxes();
        config.currentStatus = new Box<>(Status.ACTIVE);
        config.statusHistory = List.of(
                new Box<>(Status.PENDING),
                new Box<>(Status.ACTIVE),
                new Box<>(Status.COMPLETED));

        Config result = serializer.serializeFields(config, Config::inMemory);

        // Box<Status> should serialize to the enum name string
        assertEquals("ACTIVE", result.get("currentStatus"));

        // List<Box<Status>> should serialize to list of enum name strings
        List<?> serializedHistory = result.get("statusHistory");
        assertNotNull(serializedHistory);
        assertEquals(3, serializedHistory.size());
        assertEquals("PENDING", serializedHistory.get(0));
        assertEquals("ACTIVE", serializedHistory.get(1));
        assertEquals("COMPLETED", serializedHistory.get(2));
    }

    @Test
    public void testGenericBoxTypeAdapter_DeserializeEnumBox() {
        ObjectDeserializer deserializer = ObjectDeserializer.standard();
        deserializer.registerTypeAdapter(new GenericBoxTypeAdapter<Status>());

        Config config = Config.inMemory();
        config.set("currentStatus", "COMPLETED");
        config.set("statusHistory", List.of("PENDING", "CANCELLED"));

        ConfigWithEnumBoxes result = deserializer.deserializeFields(config, ConfigWithEnumBoxes::new);

        // Box<Status> should deserialize from enum name string
        assertNotNull(result.currentStatus);
        assertEquals(Status.COMPLETED, result.currentStatus.getValue());

        // List<Box<Status>> should deserialize from list of enum name strings
        assertNotNull(result.statusHistory);
        assertEquals(2, result.statusHistory.size());
        assertEquals(Status.PENDING, result.statusHistory.get(0).getValue());
        assertEquals(Status.CANCELLED, result.statusHistory.get(1).getValue());
    }

    @Test
    public void testGenericBoxTypeAdapter_RoundTripWithEnums() {
        ObjectSerializer serializer = ObjectSerializer.builder()
                .withTypeAdapter(new GenericBoxTypeAdapter<Status>())
                .build();
        ObjectDeserializer deserializer = ObjectDeserializer.builder()
                .withTypeAdapter(new GenericBoxTypeAdapter<Status>())
                .build();

        ConfigWithEnumBoxes original = new ConfigWithEnumBoxes();
        original.currentStatus = new Box<>(Status.CANCELLED);
        original.statusHistory = List.of(
                new Box<>(Status.PENDING),
                new Box<>(Status.ACTIVE),
                new Box<>(Status.CANCELLED));

        // Serialize
        Config serialized = serializer.serializeFields(original, Config::inMemory);

        // Deserialize
        ConfigWithEnumBoxes restored = deserializer.deserializeFields(serialized, ConfigWithEnumBoxes::new);

        // Verify round-trip
        assertEquals(Status.CANCELLED, restored.currentStatus.getValue());
        assertEquals(3, restored.statusHistory.size());
        assertEquals(Status.PENDING, restored.statusHistory.get(0).getValue());
        assertEquals(Status.ACTIVE, restored.statusHistory.get(1).getValue());
        assertEquals(Status.CANCELLED, restored.statusHistory.get(2).getValue());
    }

    // ============ Tests for canHandle() ============

    @Test
    public void testCanHandle_WithParameterizedType() throws NoSuchFieldException {
        BoxTypeAdapter adapter = new BoxTypeAdapter();

        // Get the generic type of Box<String> from the field declaration
        Type boxStringType = ContainerWithBox.class.getDeclaredField("stringBox").getGenericType();

        assertTrue(adapter.canHandle(boxStringType), "Should handle Box<String>");
        assertInstanceOf(ParameterizedType.class, boxStringType, "Type should be ParameterizedType");

        ParameterizedType pt = (ParameterizedType) boxStringType;
        assertEquals(Box.class, pt.getRawType(), "Raw type should be Box.class");
        assertEquals(String.class, pt.getActualTypeArguments()[0], "Type argument should be String.class");
    }

    @Test
    public void testCanHandle_WithRawClass() {
        BoxTypeAdapter adapter = new BoxTypeAdapter();
        assertTrue(adapter.canHandle(Box.class), "Should handle raw Box.class");
    }

    @Test
    public void testCanHandle_WithNonMatchingType() {
        BoxTypeAdapter adapter = new BoxTypeAdapter();
        assertFalse(adapter.canHandle(String.class), "Should not handle String.class");
        assertFalse(adapter.canHandle(List.class), "Should not handle List.class");
    }

    // ============ Tests for TypeConstraint integration ============

    @Test
    public void testTypeConstraint_ResolvesGenericsCorrectly() throws NoSuchFieldException {
        Type boxStringType = ContainerWithBox.class.getDeclaredField("stringBox").getGenericType();
        TypeConstraint constraint = new TypeConstraint(boxStringType);

        assertEquals(boxStringType, constraint.getFullType(), "Full type should match");
        assertTrue(constraint.getSatisfyingRawType().isPresent(), "Should have a raw type");
        assertEquals(Box.class, constraint.getSatisfyingRawType().get(), "Raw type should be Box.class");
    }

    @Test
    public void testTypeConstraint_WithNestedGenerics() throws NoSuchFieldException {
        Type listBoxStringType = ContainerWithBox.class.getDeclaredField("boxList").getGenericType();
        TypeConstraint constraint = new TypeConstraint(listBoxStringType);

        assertTrue(constraint.getSatisfyingRawType().isPresent(), "Should have a raw type");
        assertEquals(List.class, constraint.getSatisfyingRawType().get(), "Raw type should be List.class");

        assertInstanceOf(ParameterizedType.class, listBoxStringType);
        ParameterizedType listPt = (ParameterizedType) listBoxStringType;
        Type boxStringType = listPt.getActualTypeArguments()[0];

        assertInstanceOf(ParameterizedType.class, boxStringType, "Box<String> should be ParameterizedType");
        ParameterizedType boxPt = (ParameterizedType) boxStringType;
        assertEquals(Box.class, boxPt.getRawType(), "Inner raw type should be Box.class");
        assertEquals(String.class, boxPt.getActualTypeArguments()[0], "Inner type argument should be String.class");
    }

    // ============ Tests for TypeAdapterProvider ============

    @Test
    public void testTypeAdapterProvider_ProvidesCorrectAdapter() throws NoSuchFieldException {
        TypeAdapterProvider<Box<?>, Object> provider = type -> {
            if (type instanceof ParameterizedType pt && pt.getRawType() == Box.class) {
                return new BoxTypeAdapter();
            }
            if (type == Box.class) {
                return new BoxTypeAdapter();
            }
            return null;
        };

        Type boxStringType = ContainerWithBox.class.getDeclaredField("stringBox").getGenericType();

        TypeAdapter<Box<?>, Object> adapter = provider.provide(boxStringType);
        assertNotNull(adapter, "Provider should return an adapter for Box<String>");
        assertTrue(adapter.canHandle(boxStringType), "Returned adapter should handle Box<String>");
        assertNull(provider.provide(String.class), "Provider should return null for String.class");
    }

    // ============ Tests for ValueSerializer/ValueDeserializer interface
    // compatibility ============

    @Test
    public void testTypeAdapter_ImplementsBothInterfaces() {
        BoxTypeAdapter adapter = new BoxTypeAdapter();

        assertInstanceOf(ValueSerializer.class, adapter, "Should implement ValueSerializer");
        assertInstanceOf(ValueDeserializer.class, adapter, "Should implement ValueDeserializer");
    }

    // ============ Tests for ManuallyParameterized type construction ============

    @Test
    public void testManuallyParameterized_ForBoxString() {
        Type boxStringType = new TypeConstraint.ManuallyParameterized(Box.class, String.class);

        assertInstanceOf(ParameterizedType.class, boxStringType);
        ParameterizedType pt = (ParameterizedType) boxStringType;

        assertEquals(Box.class, pt.getRawType(), "Raw type should be Box.class");
        assertEquals(1, pt.getActualTypeArguments().length, "Should have one type argument");
        assertEquals(String.class, pt.getActualTypeArguments()[0], "Type argument should be String.class");

        BoxTypeAdapter adapter = new BoxTypeAdapter();
        assertTrue(adapter.canHandle(boxStringType), "Should handle manually created Box<String>");
    }

    @Test
    public void testManuallyParameterized_ForNestedTypes() {
        Type boxStringType = new TypeConstraint.ManuallyParameterized(Box.class, String.class);
        Type listBoxStringType = new TypeConstraint.ManuallyParameterized(List.class, boxStringType);

        assertInstanceOf(ParameterizedType.class, listBoxStringType);
        ParameterizedType listPt = (ParameterizedType) listBoxStringType;

        assertEquals(List.class, listPt.getRawType(), "Raw type should be List.class");
        assertEquals(1, listPt.getActualTypeArguments().length, "Should have one type argument");

        Type innerType = listPt.getActualTypeArguments()[0];
        assertInstanceOf(ParameterizedType.class, innerType, "Inner type should be ParameterizedType");
        ParameterizedType boxPt = (ParameterizedType) innerType;
        assertEquals(Box.class, boxPt.getRawType(), "Inner raw type should be Box.class");
        assertEquals(String.class, boxPt.getActualTypeArguments()[0], "Inner type argument should be String.class");
    }

    @Test
    public void testManuallyParameterized_Equality() throws NoSuchFieldException {
        Type manualBoxString = new TypeConstraint.ManuallyParameterized(Box.class, String.class);
        Type reflectionBoxString = ContainerWithBox.class.getDeclaredField("stringBox").getGenericType();

        assertInstanceOf(ParameterizedType.class, manualBoxString);
        assertInstanceOf(ParameterizedType.class, reflectionBoxString);

        ParameterizedType manualPt = (ParameterizedType) manualBoxString;
        ParameterizedType reflectionPt = (ParameterizedType) reflectionBoxString;

        assertEquals(manualPt.getRawType(), reflectionPt.getRawType(), "Raw types should match");
        assertArrayEquals(manualPt.getActualTypeArguments(), reflectionPt.getActualTypeArguments(),
                "Type arguments should match");
    }

    // ============ Edge case tests ============

    @Test
    public void testTypeAdapter_WithMultipleTypeParameters() {
        Type boxIntType = new TypeConstraint.ManuallyParameterized(Box.class, Integer.class);
        Type boxStringType = new TypeConstraint.ManuallyParameterized(Box.class, String.class);

        BoxTypeAdapter adapter = new BoxTypeAdapter();
        assertTrue(adapter.canHandle(boxIntType), "Should handle Box<Integer>");
        assertTrue(adapter.canHandle(boxStringType), "Should handle Box<String>");
        assertNotEquals(boxIntType, boxStringType, "Different parameterized types should not be equal");
    }

    // ============ Null Handling Tests ============

    /**
     * Config class with nullable Box fields for null handling tests.
     */
    public static class NullableBoxConfig {
        @SerdeDefault(whenValue = SerdeDefault.WhenValue.IS_NULL, cls = NullableBoxConfig.class, provider = "nullableStringProvider", phase = SerdePhase.DESERIALIZING)
        public Box<String> nullableString = null;
        public Box<Integer> nullableInteger = null;
        public Box<String> boxWithNullValue = new Box<>(null);

        public NullableBoxConfig() {
        }

        private String nullableStringProvider() {
            return null;
        }
    }

    /**
     * Config class with nullable List<Box<T>> for null handling tests.
     */
    public static class NullableListBoxConfig {
        public List<Box<String>> nullableList = null;
        public List<Box<String>> listWithValues = new java.util.ArrayList<>();

        public NullableListBoxConfig() {
        }
    }

    @Test
    public void testTypeAdapter_SerializeNullBoxField() {
        ObjectSerializer serializer = ObjectSerializer.standard();
        serializer.registerTypeAdapter(new BoxTypeAdapter());

        NullableBoxConfig config = new NullableBoxConfig();
        config.nullableString = null; // Box field is null
        config.nullableInteger = new Box<>(42); // Normal box

        Config result = serializer.serializeFields(config, Config::inMemory);

        // Null Box fields may be omitted or set to null depending on serializer
        // behavior
        assertEquals(42, result.getInt("nullableInteger"), "Non-null Box should serialize normally");
    }

    @Test
    public void testTypeAdapter_SerializeBoxContainingNullValue() {
        ObjectSerializer serializer = ObjectSerializer.standard();
        serializer.registerTypeAdapter(new BoxTypeAdapter());

        NullableBoxConfig config = new NullableBoxConfig();
        config.nullableString = new Box<>("hello");
        config.boxWithNullValue = new Box<>(null); // Box contains null

        Config result = serializer.serializeFields(config, Config::inMemory);

        assertEquals("hello", result.get("nullableString"));
        // Box containing null should serialize the null value
        assertNull(result.get("boxWithNullValue"), "Box containing null should serialize to null");
    }

    @Test
    public void testTypeAdapter_DeserializeNonNullToBoxField() {
        ObjectDeserializer deserializer = ObjectDeserializer.standard();
        deserializer.registerTypeAdapter(new BoxTypeAdapter());

        Config config = Config.inMemory();
        config.set("nullableInteger", 123);
        config.set("nullableString", "test");

        NullableBoxConfig result = deserializer.deserializeFields(config, NullableBoxConfig::new);

        // Non-null values should create Box instances
        assertNotNull(result.nullableInteger, "Non-null config value should create Box");
        assertEquals(123, result.nullableInteger.getValue());
        assertNotNull(result.nullableString, "Non-null config value should create Box");
        assertEquals("test", result.nullableString.getValue());
    }

    @Test
    public void testTypeAdapter_RoundTripWithNonNullValues() {
        ObjectSerializer serializer = ObjectSerializer.standard();
        serializer.registerTypeAdapter(new BoxTypeAdapter());

        ObjectDeserializer deserializer = ObjectDeserializer.standard();
        deserializer.registerTypeAdapter(new BoxTypeAdapter());

        NullableBoxConfig original = new NullableBoxConfig();
        original.nullableString = new Box<>("test");
        original.nullableInteger = new Box<>(999);

        // Serialize
        Config serialized = serializer.serializeFields(original, Config::inMemory);

        // Deserialize
        NullableBoxConfig restored = deserializer.deserializeFields(serialized, NullableBoxConfig::new);

        // Verify
        assertNotNull(restored.nullableString, "Box<String> should be restored");
        assertEquals("test", restored.nullableString.getValue());
        assertNotNull(restored.nullableInteger, "Box<Integer> should be restored");
        assertEquals(999, restored.nullableInteger.getValue());
    }

    @Test
    public void testTypeAdapter_NullListOfBoxes() {
        ObjectSerializer serializer = ObjectSerializer.standard();
        serializer.registerTypeAdapter(new BoxTypeAdapter());

        NullableListBoxConfig original = new NullableListBoxConfig();
        original.nullableList = null;

        // Serialize
        Config serialized = serializer.serializeFields(original, Config::inMemory);

        // Null list may be omitted or serialized as null
        assertTrue(serialized.get("nullableList") == null || !serialized.contains("nullableList"),
                "Null list should serialize to null or be omitted");
    }

    @Test
    public void testTypeAdapter_ListOfBoxesWithValues() {
        ObjectSerializer serializer = ObjectSerializer.standard();
        serializer.registerTypeAdapter(new BoxTypeAdapter());

        ObjectDeserializer deserializer = ObjectDeserializer.standard();
        deserializer.registerTypeAdapter(new BoxTypeAdapter());

        NullableListBoxConfig original = new NullableListBoxConfig();
        original.listWithValues = new java.util.ArrayList<>();
        original.listWithValues.add(new Box<>("first"));
        original.listWithValues.add(new Box<>("second"));
        original.listWithValues.add(new Box<>("third"));

        // Serialize
        Config serialized = serializer.serializeFields(original, Config::inMemory);

        List<?> serializedList = serialized.get("listWithValues");
        assertNotNull(serializedList, "List should not be null");
        assertEquals(3, serializedList.size(), "List should have 3 elements");
        assertEquals("first", serializedList.get(0));
        assertEquals("second", serializedList.get(1));
        assertEquals("third", serializedList.get(2));

        // Deserialize
        NullableListBoxConfig restored = deserializer.deserializeFields(serialized, NullableListBoxConfig::new);

        assertNotNull(restored.listWithValues);
        assertEquals(3, restored.listWithValues.size());
        assertEquals("first", restored.listWithValues.get(0).getValue());
        assertEquals("second", restored.listWithValues.get(1).getValue());
        assertEquals("third", restored.listWithValues.get(2).getValue());
    }

    @Test
    public void testGenericBoxTypeAdapter_RoundTripWithEnumValue() {
        ObjectSerializer serializer = ObjectSerializer.standard();
        serializer.registerTypeAdapter(new GenericBoxTypeAdapter<Status>());

        ObjectDeserializer deserializer = ObjectDeserializer.standard();
        deserializer.registerTypeAdapter(new GenericBoxTypeAdapter<Status>());

        ConfigWithEnumBoxes original = new ConfigWithEnumBoxes();
        original.currentStatus = new Box<>(Status.COMPLETED);

        // Serialize
        Config serialized = serializer.serializeFields(original, Config::inMemory);

        assertEquals("COMPLETED", serialized.get("currentStatus"), "Enum should serialize to name");

        // Deserialize
        ConfigWithEnumBoxes restored = deserializer.deserializeFields(serialized, ConfigWithEnumBoxes::new);

        assertNotNull(restored.currentStatus, "Box should be restored");
        assertEquals(Status.COMPLETED, restored.currentStatus.getValue(), "Enum value should be restored");
    }
}
