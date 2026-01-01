package re.neotamia.nightconfig.core.serde;

import org.junit.jupiter.api.Test;
import re.neotamia.nightconfig.core.Config;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Box<T> {
    public T value;

    public Box() {}
    public Box(T value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Box<?> box = (Box<?>) o;
        return Objects.equals(value, box.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

class Container {
    public Box<String> box;
}

class ListContainer {
    public List<Box<String>> boxes;
}

class GenericContainer<T> {
    public List<T> items;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericContainer<?> that = (GenericContainer<?>) o;
        return Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }
}

class BoxSerializer implements ValueSerializer<Box<String>, String> {
    @Override
    public String serialize(Box<String> value, SerializerContext ctx) {
        return "Boxed:" + value.value;
    }
}

class BoxDeserializer implements ValueDeserializer<String, Box<String>> {
    @Override
    public Box<String> deserialize(String value, TypeConstraint resultType, DeserializerContext ctx) {
        if (value.startsWith("Boxed:")) {
            return new Box<>(value.substring(6));
        }
        return new Box<>(value);
    }
}

interface TypeAdapter<T, R> extends ValueSerializer<T, R>, ValueDeserializer<R, T> {
    Class<T> valueClass();
    Class<R> resultClass();
}

class BoxTypeAdapter implements TypeAdapter<Box<String>, String> {
    @Override
    public String serialize(Box<String> value, SerializerContext ctx) {
        return "Boxed:" + value.value;
    }

    @Override
    public Box<String> deserialize(String value, TypeConstraint resultType, DeserializerContext ctx) {
        if (value.startsWith("Boxed:")) {
            return new Box<>(value.substring(6));
        }
        return new Box<>(value);
    }

    @Override
    public Class<Box<String>> valueClass() {
        return (Class)Box.class;
    }

    @Override
    public Class<String> resultClass() {
        return String.class;
    }
}

interface GenericTypeAdapter<T, R> extends ValueSerializer<T, R>, ValueDeserializer<R, T> {
    Type valueType();
    Type resultType();
}

class BoxGenericTypeAdapter implements GenericTypeAdapter<Box<String>, String> {
    private final Type boxStringType = new TypeConstraint.ManuallyParameterized(Box.class, String.class);

    @Override
    public Type valueType() {
        return boxStringType;
    }

    @Override
    public Type resultType() {
        return String.class;
    }

    @Override
    public String serialize(Box<String> value, SerializerContext ctx) {
        return "GenericBoxed:" + value.value;
    }

    @Override
    public Box<String> deserialize(String value, TypeConstraint resultType, DeserializerContext ctx) {
        return new Box<>(value.replace("GenericBoxed:", ""));
    }
}

public class GenericsTest {
    @Test
    public void testGenericSerialization() {
        ObjectSerializer serializer = ObjectSerializer.builder()
                .withSerializerProvider((type, ctx) -> {
                    if (type instanceof ParameterizedType pt) {
                        if (pt.getRawType() == Box.class && pt.getActualTypeArguments()[0] == String.class) {
                            return (value, ctx1) -> "BoxedString:" + ((Box<String>) value).value;
                        }
                    }
                    return null;
                })
                .build();

        Box<String> stringBox = new Box<>("hello");

        // If we have it in a field:
        Container container = new Container();
        container.box = stringBox;

        Config result = serializer.serializeFields(container, Config::inMemory);
        assertEquals("BoxedString:hello", result.get("box"));
    }

    @Test
    public void testGenericDeserialization() {
        ObjectDeserializer deserializer = ObjectDeserializer.builder()
                .withDeserializerForType(String.class, new TypeConstraint.ManuallyParameterized(Box.class, String.class), (value, type, ctx) -> {
                    return new Box<>((String) value);
                })
                .build();

        Config config = Config.inMemory();
        config.set("box", "hello");

        Container result = deserializer.deserializeFields(config, Container::new);
        assertEquals(new Box<>("hello"), result.box);
    }

    @Test
    public void testCollectionOfGenericTypeSerialization() {
        ObjectSerializer serializer = ObjectSerializer.builder()
                .withSerializerProvider((type, ctx) -> {
                    if (type instanceof ParameterizedType pt) {
                        if (pt.getRawType() == Box.class && pt.getActualTypeArguments()[0] == String.class) {
                            return (ValueSerializer<Box<String>, String>) (value, ctx1) -> "Boxed:" + value.value;
                        }
                    }
                    return null;
                })
                .build();

        ListContainer container = new ListContainer();
        container.boxes = Arrays.asList(new Box<>("one"), new Box<>("two"));

        Config result = serializer.serializeFields(container, Config::inMemory);
        assertEquals(Arrays.asList("Boxed:one", "Boxed:two"), result.get("boxes"));
    }

    @Test
    public void testCollectionOfGenericTypeDeserialization() {
        ObjectDeserializer deserializer = ObjectDeserializer.builder()
                .withDeserializerForType(String.class, new TypeConstraint.ManuallyParameterized(Box.class, String.class), (value, type, ctx) -> {
                    return new Box<>((String) value);
                })
                .build();

        Config config = Config.inMemory();
        config.set("boxes", Arrays.asList("one", "two"));

        ListContainer result = deserializer.deserializeFields(config, ListContainer::new);
        assertEquals(Arrays.asList(new Box<>("one"), new Box<>("two")), result.boxes);
    }

    @Test
    public void testGenericClassWithCollectionSerialization() {
        ObjectSerializer serializer = ObjectSerializer.builder()
                .withSerializerProvider((type, ctx) -> {
                    if (type instanceof ParameterizedType pt) {
                        if (pt.getRawType() == Box.class && pt.getActualTypeArguments()[0] == String.class) {
                            return (ValueSerializer<Box<String>, String>) (value, ctx1) -> "Boxed:" + value.value;
                        }
                    }
                    return null;
                })
                .build();

        GenericContainer<Box<String>> container = new GenericContainer<Box<String>>() {};
        container.items = Arrays.asList(new Box<>("a"), new Box<>("b"));

        Config result = serializer.serializeFields(container, Config::inMemory);
        assertEquals(Arrays.asList("Boxed:a", "Boxed:b"), result.get("items"));
    }

    @Test
    public void testGenericClassWithCollectionDeserialization() {
        ObjectDeserializer deserializer = ObjectDeserializer.builder()
                .withDeserializerForType(String.class, new TypeConstraint.ManuallyParameterized(Box.class, String.class), (value, type, ctx) -> {
                    return new Box<>((String) value);
                })
                .build();

        Config config = Config.inMemory();
        config.set("items", Arrays.asList("one", "two"));

        GenericContainer<Box<String>> result = deserializer.deserializeFields(config, () -> new GenericContainer<Box<String>>() {});
        assertEquals(Arrays.asList(new Box<>("one"), new Box<>("two")), result.items);
    }

    @Test
    public void testFormalInterfaceSerde() throws NoSuchFieldException {
        Type boxStringType = Container.class.getField("box").getGenericType();
        Type manualType = new TypeConstraint.ManuallyParameterized(Box.class, String.class);
        assertEquals(manualType, boxStringType, "Types should be equal");
        assertEquals(manualType.hashCode(), boxStringType.hashCode(), "HashCodes should be equal");

        ObjectSerializer serializer = ObjectSerializer.builder()
                .withSerializerForType(manualType, new BoxSerializer())
                .build();

        Box<String> box = new Box<>("hello");
        Container c = new Container();
        c.box = box;
        Config result = serializer.serializeFields(c, Config::inMemory);
        assertEquals("Boxed:hello", result.get("box"));

        ObjectDeserializer deserializer = ObjectDeserializer.builder()
                .withDeserializerForType(String.class, manualType, new BoxDeserializer())
                .build();

        Config config = Config.inMemory();
        config.set("box", "Boxed:hello");
        Container container = deserializer.deserializeFields(config, Container::new);
        assertEquals(new Box<>("hello"), container.box);
    }

    @Test
    public void testTypeAdapterPattern() {
        ObjectSerializer serializer = ObjectSerializer.standard();
        ObjectDeserializer deserializer = ObjectDeserializer.standard();

        BoxTypeAdapter adapter = new BoxTypeAdapter();
        serializer.registerSerializerForClass(adapter.valueClass(), adapter);
        deserializer.registerDeserializerForClass(adapter.resultClass(), adapter.valueClass(), adapter);

        // Test serialization
        Container c = new Container();
        c.box = new Box<>("world");
        Config result = serializer.serializeFields(c, Config::inMemory);
        assertEquals("Boxed:world", result.get("box"));

        // Test deserialization
        Config config = Config.inMemory();
        config.set("box", "Boxed:universe");
        Container deserialized = deserializer.deserializeFields(config, Container::new);
        assertEquals(new Box<>("universe"), deserialized.box);
    }

    @Test
    public void testGenericTypeAdapterPattern() {
        ObjectSerializer serializer = ObjectSerializer.standard();
        ObjectDeserializer deserializer = ObjectDeserializer.standard();

        BoxGenericTypeAdapter adapter = new BoxGenericTypeAdapter();
        serializer.registerSerializerForType(adapter.valueType(), adapter);
        deserializer.registerDeserializerForType((Class)adapter.resultType(), adapter.valueType(), adapter);

        // Test serialization
        Container c = new Container();
        c.box = new Box<>("world");
        Config result = serializer.serializeFields(c, Config::inMemory);
        assertEquals("GenericBoxed:world", result.get("box"));

        // Test deserialization
        Config config = Config.inMemory();
        config.set("box", "GenericBoxed:universe");
        Container deserialized = deserializer.deserializeFields(config, Container::new);
        assertEquals(new Box<>("universe"), deserialized.box);
    }
}
