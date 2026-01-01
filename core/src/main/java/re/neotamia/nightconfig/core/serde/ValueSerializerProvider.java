package re.neotamia.nightconfig.core.serde;

import java.lang.reflect.Type;

/**
 * Provides {@link ValueSerializer} to serialize configuration values.
 * <p>
 * The {@link #provide(Type, SerializerContext)} method returns {@code null} when
 * it cannot provide a serializer for the given value type.
 * In that case, other providers will be called, until a suitable serializer is found
 * or all the providers have been tried.
 * 
 * @param <V> type of the config values to serialize
 * @param <R> resulting type of the deserialization of these values
 */
@FunctionalInterface
public interface ValueSerializerProvider<V, R> {
    /**
     * Provides a serializer for a value of type {@code valueType}.
     * The returned serializer must be able to handle a value of this
     * type.
     * 
     * @param valueType type of the config values to serialize
     * @return a serializer, or {@code null} to try the next provider
     * @see ValueSerializerProvider
     */
    ValueSerializer<V, R> provide(Type valueType, SerializerContext ctx);
}
