package re.neotamia.nightconfig.core.serde;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * Provides {@link TypeAdapter} instances for specific types.
 * <p>
 * Unlike {@link ValueSerializerProvider} and {@link ValueDeserializerProvider}
 * which use {@code Class<?>},
 * this provider uses {@link Type} to support precise generic type matching,
 * including {@link java.lang.reflect.ParameterizedType} for types like
 * {@code List<Box<String>>}.
 * <p>
 * The {@link #provide(Type)} method returns {@code null} when it cannot provide
 * an adapter for the given type.
 * In that case, other providers will be called, until a suitable adapter is
 * found or all providers have been tried.
 *
 * @param <J> the Java type that adapters from this provider handle
 * @param <C> the config value type
 */
@FunctionalInterface
public interface TypeAdapterProvider<J, C> {

    /**
     * Provides a type adapter for the given type.
     * <p>
     * The type can be:
     * <ul>
     * <li>{@link Class} - a simple class type like {@code String.class}</li>
     * <li>{@link java.lang.reflect.ParameterizedType} - a generic type like
     * {@code List<String>}</li>
     * <li>{@link java.lang.reflect.GenericArrayType} - a generic array type like
     * {@code T[]}</li>
     * <li>{@link java.lang.reflect.WildcardType} - a wildcard type like
     * {@code ? extends Number}</li>
     * <li>{@link java.lang.reflect.TypeVariable} - a type variable like
     * {@code T}</li>
     * </ul>
     *
     * @param type the full type including generic information
     * @return a type adapter, or {@code null} to try the next provider
     */
    @Nullable
    TypeAdapter<J, C> provide(Type type);
}
