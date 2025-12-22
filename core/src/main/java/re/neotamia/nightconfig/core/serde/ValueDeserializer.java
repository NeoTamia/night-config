package re.neotamia.nightconfig.core.serde;

import org.jetbrains.annotations.Nullable;

/** Turns a config value of type {@code T} into a Java object of type {@code R}. */
public interface ValueDeserializer<T, R> {
	R deserialize(T value, @Nullable TypeConstraint resultType, DeserializerContext ctx);
}
