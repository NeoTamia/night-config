package re.neotamia.nightconfig.core.serde;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Turns a Java object of type {@code T} into a Config value of type {@code R}. */
public interface ValueSerializer<T, R> {
   @Nullable R serialize(T value, @NotNull SerializerContext ctx);
}
