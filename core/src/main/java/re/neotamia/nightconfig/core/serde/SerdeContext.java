package re.neotamia.nightconfig.core.serde;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import re.neotamia.nightconfig.core.Config;

/**
 * Context that holds ObjectSerializer and ObjectDeserializer for type-aware
 * config operations. Each instance is independent - no static state.
 * <p>
 * This allows TypeAdapters to be used with {@link Config#setTyped} and
 * {@link re.neotamia.nightconfig.core.UnmodifiableConfig#getTyped} methods.
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 * SerdeContext ctx = SerdeContext.builder()
 *         .withTypeAdapter(new ResourceLocationTypeAdapter())
 *         .build();
 * 
 * config.setSerdeContext(ctx);
 * config.setTyped("location", new ResourceLocation("minecraft:stone"));
 * ResourceLocation loc = config.getTyped("location", ResourceLocation.class);
 * }</pre>
 */
public class SerdeContext {
    private final ObjectSerializer serializer;
    private final ObjectDeserializer deserializer;

    /**
     * Creates a new SerdeContext with the given serializer and deserializer.
     *
     * @param serializer   the serializer to use
     * @param deserializer the deserializer to use
     */
    public SerdeContext(@NotNull ObjectSerializer serializer, @NotNull ObjectDeserializer deserializer) {
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    /**
     * Creates a new builder for SerdeContext.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the ObjectSerializer used by this context.
     *
     * @return the serializer
     */
    public ObjectSerializer getSerializer() {
        return serializer;
    }

    /**
     * Returns the ObjectDeserializer used by this context.
     *
     * @return the deserializer
     */
    public ObjectDeserializer getDeserializer() {
        return deserializer;
    }

    /**
     * Registers a TypeAdapter to both the serializer and deserializer.
     *
     * @param adapter the type adapter to register
     * @param <J>     the Java type the adapter handles
     * @param <C>     the config value type
     */
    public <J, C> void registerTypeAdapter(@NotNull TypeAdapter<J, C> adapter) {
        serializer.registerTypeAdapter(adapter);
        deserializer.registerTypeAdapter(adapter);
    }

    /**
     * Serializes a value to a config-compatible form.
     *
     * @param value the value to serialize
     * @return the serialized value, or null if the input was null
     */
    public @Nullable Object serialize(@Nullable Object value) {
        if (value == null) {
            return null;
        }
        return serializer.serialize(value, Config::inMemory);
    }

    /**
     * Deserializes a raw config value to the target type.
     *
     * @param rawValue the raw value from config
     * @param type     the target type class
     * @param <T>      the target type
     * @return the deserialized value, or null if the raw value was null
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable T deserialize(@Nullable Object rawValue, @NotNull Class<T> type) {
        if (rawValue == null) {
            return null;
        }
        DeserializerContext ctx = new DeserializerContext(deserializer);
        return (T) ctx.deserializeValue(rawValue, new TypeConstraint(type));
    }

    /**
     * Builder for SerdeContext.
     */
    public static class Builder {
        private final ObjectSerializerBuilder serializerBuilder = ObjectSerializer.builder();
        private final ObjectDeserializerBuilder deserializerBuilder = ObjectDeserializer.builder();

        /**
         * Registers a TypeAdapter for both serialization and deserialization.
         *
         * @param adapter the type adapter to register
         * @param <J>     the Java type the adapter handles
         * @param <C>     the config value type
         * @return this builder
         */
        public <J, C> Builder withTypeAdapter(TypeAdapter<J, C> adapter) {
            serializerBuilder.withTypeAdapter(adapter);
            deserializerBuilder.withTypeAdapter(adapter);
            return this;
        }

        /**
         * Sets the naming strategy for both serializer and deserializer.
         *
         * @param strategy the naming strategy to use
         * @return this builder
         */
        public Builder withNamingStrategy(NamingStrategy strategy) {
            serializerBuilder.withNamingStrategy(strategy);
            deserializerBuilder.withNamingStrategy(strategy);
            return this;
        }

        /**
         * Builds the SerdeContext.
         *
         * @return a new SerdeContext
         */
        public SerdeContext build() {
            return new SerdeContext(serializerBuilder.build(), deserializerBuilder.build());
        }
    }
}
