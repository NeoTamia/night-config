package re.neotamia.nightconfig.core.serde;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Turns a config value of type {@code T} into a Java object of type {@code R}. */
public interface ValueDeserializer<T, R> {
	@NotNull R deserialize(T value, @Nullable TypeConstraint resultType, @NotNull DeserializerContext ctx);
}
