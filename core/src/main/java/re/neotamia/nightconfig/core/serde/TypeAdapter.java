package re.neotamia.nightconfig.core.serde;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * A type adapter that handles both serialization and deserialization for a
 * specific type.
 * This interface combines {@link ValueSerializer} and {@link ValueDeserializer}
 * functionality,
 * with support for precise generic type handling via {@link Type} and
 * {@link java.lang.reflect.ParameterizedType}.
 *
 * <p>
 * Example for handling {@code Box<String>}:
 * 
 * <pre>
 * {
 *     public class BoxTypeAdapter<T> implements TypeAdapter<Box<T>, Object> {
 *         &#64;Override
 *         public boolean canHandle(Type type) {
 *             if (type instanceof ParameterizedType pt) {
 *                 return pt.getRawType() == Box.class;
 *             }
 *             return type == Box.class;
 *         }
 *
 *         &#64;Override
 *         public Object serialize(Box<T> value, Type type, SerializerContext ctx) {
 *             return ctx.serializeValue(value.getValue());
 *         }
 *
 *         @Override
 *         public Box<T> deserialize(Object value, Type type, DeserializerContext ctx) {
 *             Type valueType = ((ParameterizedType) type).getActualTypeArguments()[0];
 *             T inner = ctx.deserializeValue(value, new TypeConstraint(valueType));
 *             return new Box<>(inner);
 *         }
 *     }
 * }
 * </pre>
 *
 * @param <J> the Java type this adapter handles (e.g., {@code Box<T>})
 * @param <C> the config value type (e.g., Object, String, Number)
 */
public interface TypeAdapter<J, C> extends ValueSerializer<J, C>, ValueDeserializer<C, J> {

    /**
     * Checks if this adapter can handle the given type.
     * This allows checking for generic types like {@code Box<String>} or
     * {@code List<Box<Integer>>}.
     *
     * @param type the type to check (can be Class, ParameterizedType,
     *             GenericArrayType, etc.)
     * @return true if this adapter can handle the type
     */
    boolean canHandle(Type type);

    /**
     * Serializes a Java object to a configuration value with full type information.
     * The type parameter provides the declared generic type.
     *
     * @param value the value to serialize
     * @param type  the declared type (may be a ParameterizedType with generic info)
     * @param ctx   the serializer context
     * @return the serialized config value
     */
    C serialize(J value, Type type, SerializerContext ctx);

    /**
     * Deserializes a config value to a Java object with full type information.
     *
     * @param value the config value to deserialize
     * @param type  the target type (may be a ParameterizedType with generic info)
     * @param ctx   the deserializer context
     * @return the deserialized Java object
     */
    J deserialize(C value, Type type, DeserializerContext ctx);

    // Default implementations to satisfy ValueSerializer and ValueDeserializer

    @Override
    default C serialize(J value, SerializerContext ctx) {
        // Fallback: use value's runtime class if type isn't provided
        return serialize(value, value.getClass(), ctx);
    }

    @Override
    default J deserialize(C value, @Nullable TypeConstraint resultType, DeserializerContext ctx) {
        Type type = (resultType != null) ? resultType.getFullType() : Object.class;
        return deserialize(value, type, ctx);
    }
}
