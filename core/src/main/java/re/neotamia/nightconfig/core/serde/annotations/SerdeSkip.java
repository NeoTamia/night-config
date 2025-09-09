package re.neotamia.nightconfig.core.serde.annotations;

import re.neotamia.nightconfig.core.UnmodifiableConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @see SerdeSkipDeserializingIf
 * @see SerdeSkipSerializingIf
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SerdeSkip {
    /**
     * The type of skip predicate: either a predefined condition, or {@code CUSTOM}.
     * <p>
     * If set to {@code CUSTOM}, you must provide the {@link #customCheck()}
     * parameter, and you may provide the {@link #customClass()} parameter.
     */
    SkipIf[] value();

    /**
     * The class where to find the custom skip predicate.
     * <p>
     * By default, it is set to {@code Object.class}, which is treated as a special
     * value.
     * It means that the actual class to use is the one of the object that is
     * currently being (de)serialized.
     *
     * @return the class that defines the skip predicate
     * @see #customCheck()
     */
    Class<?> customClass() default Object.class;

    /**
     * The name of the field or method that defines the predicate to apply
     * in order to test whether the field that we are deserializing should
     * be skipped. The predicate is applied on the raw config value,
     * as returned by {@link UnmodifiableConfig#getRaw(java.util.List)}.
     * <p>
     * <strong>Constraints on methods</strong>
     * The predicate method must take exactly one parameter of type {@code Object}.
     * If {@link #customClass()} is set to its non-default value, the method must be static.
     * <p>
     * <strong>Constraints on fields</strong>
     * The predicate field must be of type {@code java.util.function.Predicate<Object>}.
     * In most cases, the predicate field should be declared with the
     * {@code transient} keyword, to prevent it from being (de)serialized.
     *
     * @return the name of the skip predicate
     * @see java.util.function.Predicate
     */
    String customCheck() default "";

    /**
     * A condition that defines when to skip the field during (de)serialization.
     * <p>
     * The field is skipped if the condition is true.
     */
    enum SkipIf {
        /**
         * Always skip the field.
         */
        ALWAYS,
        /**
         * Skip the field if the corresponding config value is null.
         */
        IS_NULL,
        /**
         * Skip the field if the corresponding config value is empty.
         * <p>
         * Determining whether an object is "empty" or not is done in a "logical" way
         * for common Java objects.
         * For instance, a {@code CharSequence} is empty is its {@code length()} is
         * zero, a {@code Collection} is empty if calling {@code isEmpty()} returns
         * true, etc.
         * As a last-resort try to implement the "is empty" check, reflection is used to
         * find and call the method {@code boolean isEmpty()} on the value.
         */
        IS_EMPTY,
        /**
         * Skip the field if the corresponding config value satisfies a custom
         * condition, defined by {@link SerdeSkip#customCheck()}.
         */
        CUSTOM,
    }
}
