package re.neotamia.nightconfig.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import re.neotamia.nightconfig.core.concurrent.ConcurrentConfig;

import java.util.*;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import static re.neotamia.nightconfig.core.NullObject.NULL_OBJECT;
import static re.neotamia.nightconfig.core.utils.StringUtils.split;

/**
 * An unmodifiable (read-only) configuration that contains key/value mappings.
 *
 * @author TheElectronWill
 */
public interface UnmodifiableConfig {
    /**
     * Gets a value from the config.
     *
     * @param path the value's path, each part separated by a dot. Example "a.b.c"
     * @param <T>  the value's type
     * @return the value at the given path, or {@code null} if there is no such value.
     */
    default <T> @Nullable T get(@NotNull String path) {
        return get(split(path, '.'));
    }

    /**
     * Gets a value from the config.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @param <T>  the value's type
     * @return the value at the given path, or {@code null} if there is no such value.
     */
    @SuppressWarnings("unchecked")
    default <T> @Nullable T get(@NotNull List<String> path) {
        Object raw = getRaw(path);
        return (raw == NULL_OBJECT) ? null : (T) raw;
    }

    /**
     * Gets a value from the config. Doesn't convert {@link NullObject#NULL_OBJECT} to {@code null}.
     *
     * @param path the value's path, each part separated by a dot. Example "a.b.c"
     * @param <T>  the value's type
     * @return the value at the given path, or {@code null} if there is no such value.
     */
    default <T> @Nullable T getRaw(@NotNull String path) {
        return getRaw(split(path, '.'));
    }

    /**
     * Gets a value from the config. Doesn't convert {@link NullObject#NULL_OBJECT} to {@code null}.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @param <T>  the value's type
     * @return the value at the given path, or {@code null} if there is no such value.
     */
    <T> @Nullable T getRaw(@NotNull List<String> path);

    /**
     * Gets an optional value from the config.
     *
     * @param path the value's path, each part separated by a dot. Example "a.b.c"
     * @param <T>  the value's type
     * @return an Optional containing the value at the given path, or {@code Optional.empty()} if
     * there is no such value.
     */
    default <T> @NotNull Optional<T> getOptional(@NotNull String path) {
        return getOptional(split(path, '.'));
    }

    /**
     * Gets an optional value from the config.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @param <T>  the value's type
     * @return an Optional containing the value at the given path, or {@code Optional.empty()} if
     * there is no such value.
     */
    default <T> @NotNull Optional<T> getOptional(@NotNull List<String> path) {
        return Optional.ofNullable(get(path));
    }

    /**
     * Gets a value from the config. If the value doesn't exist, returns the default value.
     *
     * @param path         the value's path, each part separated by a dot. Example "a.b.c"
     * @param defaultValue the default value to return if not found
     * @param <T>          the value's type
     * @return the value at the given path, or the default value if not found.
     */
    default <T> T getOrElse(@NotNull String path, T defaultValue) {
        return getOrElse(split(path, '.'), defaultValue);
    }

    /**
     * Gets a value from the config. If the value doesn't exist, returns the default value.
     *
     * @param path         the value's path, each element of the list is a different part of the path.
     * @param defaultValue the default value to return if not found
     * @param <T>          the value's type
     * @return the value at the given path, or the default value if not found.
     */
    default <T> T getOrElse(@NotNull List<String> path, T defaultValue) {
        T value = getRaw(path);
        return (value == null || value == NULL_OBJECT) ? defaultValue : value;
    }

    /**
     * Gets a value from the config. If the value doesn't exist, returns the default value.
     *
     * @param path                 the value's path, each element of the list is a different part of the path.
     * @param defaultValueSupplier the Supplier of the default value
     * @param <T>                  the value's type
     * @return the value at the given path, or the default value if not found.
     */
    default <T> T getOrElse(@NotNull List<String> path, @NotNull Supplier<T> defaultValueSupplier) {
        T value = getRaw(path);
        return (value == null || value == NULL_OBJECT) ? defaultValueSupplier.get() : value;
    }

    /**
     * Gets a value from the config. If the value doesn't exist, returns the default value.
     *
     * @param path                 the value's path, each part separated by a dot. Example "a.b.c"
     * @param defaultValueSupplier the Supplier of the default value
     * @param <T>                  the value's type
     * @return the value at the given path, or the default value if not found.
     */
    default <T> T getOrElse(@NotNull String path, @NotNull Supplier<T> defaultValueSupplier) {
        return getOrElse(split(path, '.'), defaultValueSupplier);
    }

    // ---- String getters ----

    /**
     * Like {@link #get(String)} but returns a String. The config's value must be a String.
     * Throws an exception if the value does not exist.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     * @return the value at the given path, as a String.
     */
    default @Nullable String getString(@NotNull String path) {
        return this.get(path);
    }

    /**
     * Like {@link #get(List)} but returns a String. The config's value must be a String.
     * Throws an exception if the value does not exist.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @return the value at the given path, as a String.
     */
    default @Nullable String getString(@NotNull List<String> path) {
        return this.get(path);
    }

    /**
     * Like {@link #get(String)} but returns a Float. The config's value must be a String
     * that can be parsed as a Float. Throws an exception if the value does not exist.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     * @return the value at the given path, parsed as a Float.
     */
    default @NotNull Float getStringAsFloat(@NotNull String path) {
        return this.getStringAsFloat(split(path, '.'));
    }

    /**
     * Like {@link #get(List)} but returns a Float. The config's value must be a String
     * that can be parsed as a Float. Throws an exception if the value does not exist.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @return the value at the given path, parsed as a Float.
     */
    default @NotNull Float getStringAsFloat(@NotNull List<String> path) {
        return Float.parseFloat(this.get(path));
    }

    /**
     * Like {@link #get(String)} but returns a Double. The config's value must be a String
     * that can be parsed as a Double. Throws an exception if the value does not exist.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     * @return the value at the given path, parsed as a Double.
     */
    default @NotNull Double getStringAsDouble(@NotNull String path) {
        return this.getStringAsDouble(split(path, '.'));
    }

    /**
     * Like {@link #get(List)} but returns a Double. The config's value must be a String
     * that can be parsed as a Double. Throws an exception if the value does not exist.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @return the value at the given path, parsed as a Double.
     */
    default @NotNull Double getStringAsDouble(@NotNull List<String> path) {
        return Double.parseDouble(this.get(path));
    }

    /**
     * Like {@link #get(String)} but returns an Int. The config's value must be a String
     * that can be parsed as an Int. Throws an exception if the value does not exist.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     * @return the value at the given path, parsed as an Int.
     */
    default @NotNull Short getStringAsShort(@NotNull String path) {
        return this.getStringAsShort(split(path, '.'));
    }

    /**
     * Like {@link #get(List)} but returns a Short. The config's value must be a String
     * that can be parsed as a Short. Throws an exception if the value does not exist.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @return the value at the given path, parsed as a Short.
     */
    default @NotNull Short getStringAsShort(@NotNull List<String> path) {
        return Short.parseShort(this.get(path));
    }

    /**
     * Like {@link #get(String)} but returns a Byte. The config's value must be a String
     * that can be parsed as a Byte. Throws an exception if the value does not exist.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     * @return the value at the given path, parsed as a Byte.
     */
    default @NotNull Byte getStringAsByte(@NotNull String path) {
        return this.getStringAsByte(split(path, '.'));
    }

    /**
     * Like {@link #get(List)} but returns a Byte. The config's value must be a String
     * that can be parsed as a Byte. Throws an exception if the value does not exist.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @return the value at the given path, parsed as a Byte.
     */
    default @NotNull Byte getStringAsByte(@NotNull List<String> path) {
        return Byte.parseByte(this.get(path));
    }

    /**
     * Like {@link #get(String)} but returns an Integer. The config's value must be a String
     * that can be parsed as an Integer. Throws an exception if the value does not exist.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     * @return the value at the given path, parsed as an Integer.
     */
    default @NotNull Integer getStringAsInteger(@NotNull String path) {
        return this.getStringAsInteger(split(path, '.'));
    }

    /**
     * Like {@link #get(List)} but returns an Integer. The config's value must be a String
     * that can be parsed as an Integer. Throws an exception if the value does not exist.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @return the value at the given path, parsed as an Integer.
     */
    default @NotNull Integer getStringAsInteger(@NotNull List<String> path) {
        return Integer.parseInt(this.get(path));
    }

    /**
     * Like {@link #get(String)} but returns a Long. The config's value must be a String
     * that can be parsed as a Long. Throws an exception if the value does not exist.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     * @return the value at the given path, parsed as a Long.
     */
    default @NotNull Long getStringAsLong(@NotNull String path) {
        return this.getStringAsLong(split(path, '.'));
    }

    /**
     * Like {@link #get(List)} but returns a Long. The config's value must be a String
     * that can be parsed as a Long. Throws an exception if the value does not exist.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @return the value at the given path, parsed as a Long.
     */
    default @NotNull Long getStringAsLong(@NotNull List<String> path) {
        return Long.parseLong(this.get(path));
    }

    /**
     * Like {@link #get(String)} but returns a Boolean. The config's value must be a String
     * that can be parsed as a Boolean. Throws an exception if the value does not exist.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     * @return the value at the given path, parsed as a Boolean.
     */
    default @NotNull Boolean getStringAsBoolean(@NotNull String path) {
        return this.getStringAsBoolean(split(path, '.'));
    }

    /**
     * Like {@link #get(List)} but returns a Boolean. The config's value must be a String
     * that can be parsed as a Boolean. Throws an exception if the value does not exist.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @return the value at the given path, parsed as a Boolean.
     */
    default @NotNull Boolean getStringAsBoolean(@NotNull List<String> path) {
        return Boolean.parseBoolean(this.get(path));
    }

    /**
     * Gets an optional String from the config.
     *
     * @param path the value's path, each part separated by a dot. Example "a.b.c"
     * @return an Optional containing the value at the given path as a String,
     * or {@code Optional.empty()} if there is no such value.
     */
    default @NotNull Optional<String> getOptionalString(@NotNull String path) {
        return getOptionalString(split(path, '.'));
    }

    /**
     * Gets an optional String from the config.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @return an Optional containing the value at the given path as a String,
     * or {@code Optional.empty()} if there is no such value.
     */
    default @NotNull Optional<String> getOptionalString(@NotNull List<String> path) {
        return Optional.ofNullable(getString(path));
    }

    /**
     * Gets a String from the config. If the value doesn't exist, returns the default value.
     *
     * @param path         the value's path, each part separated by a dot. Example "a.b.c"
     * @param defaultValue the default value to return if not found
     * @return the value at the given path as a String, or the default value if not found.
     */
    default String getStringOrElse(@NotNull String path, String defaultValue) {
        return getStringOrElse(split(path, '.'), defaultValue);
    }

    /**
     * Gets a String from the config. If the value doesn't exist, returns the default value.
     *
     * @param path         the value's path, each element of the list is a different part of the path.
     * @param defaultValue the default value to return if not found
     * @return the value at the given path as a String, or the default value if not found.
     */
    default String getStringOrElse(@NotNull List<String> path, String defaultValue) {
        String value = getString(path);
        return (value == null) ? defaultValue : value;
    }

    // ---- Enum getters ----

    /**
     * Gets an Enum value from the config. If the value doesn't exist, returns null.
     *
     * @param path     the value's path, each part separated by a dot. Example "a.b.c"
     * @param enumType the class of the Enum
     * @param method   the method to use when converting a non-enum value like a String or an int
     * @param <T>      the value's type
     * @return the value at the given path as an enum, or {@code null} if not found.
     * @throws IllegalArgumentException if the config contains a String that doesn't match any of
     *                                  the enum constants, with regards to the given method
     * @throws ClassCastException       if the config contains a value that cannot be converted to
     *                                  an enum constant, like a List
     */
    default <T extends Enum<T>> @Nullable T getEnum(@NotNull String path, @NotNull Class<T> enumType, @NotNull EnumGetMethod method) {
        return getEnum(split(path, '.'), enumType, method);
    }

    /**
     * Calls {@link #getEnum(String, Class, EnumGetMethod)} with method
     * {@link EnumGetMethod#NAME_IGNORECASE}.
     *
     * @param path     the value's path, each part separated by a dot. Example "a.b.c"
     * @param enumType the class of the Enum
     * @param <T>      the enum type
     * @return the value at the given path as an enum, or {@code null} if not found
     */
    default <T extends Enum<T>> @Nullable T getEnum(@NotNull String path, @NotNull Class<T> enumType) {
        return getEnum(split(path, '.'), enumType, EnumGetMethod.NAME_IGNORECASE);
    }

    /**
     * Gets an Enum value from the config. If the value doesn't exist, returns null.
     *
     * @param path     the value's path, each element of the list is a different part of the path.
     * @param enumType the class of the Enum
     * @param method   the method to use when converting a non-enum value like a String or an int
     * @param <T>      the value's type
     * @return the value at the given path as an enum, or {@code null} if not found.
     * @throws IllegalArgumentException if the config contains a String that doesn't match any of
     *                                  the enum constants, with regards to the given method
     * @throws ClassCastException       if the config contains a value that cannot be converted to
     *                                  an enum constant, like a List
     */
    default <T extends Enum<T>> @Nullable T getEnum(@NotNull List<String> path, @NotNull Class<T> enumType, @NotNull EnumGetMethod method) {
        final Object value = getRaw(path);
        return method.get(value, enumType);
    }

    /**
     * Calls {@link #getEnum(List, Class, EnumGetMethod)} with method
     * {@link EnumGetMethod#NAME_IGNORECASE}.
     *
     * @param path     the value's path, each element of the list is a different part of the path.
     * @param enumType the class of the Enum
     * @return the value at the given path as an enum, or {@code null} if not found.
     */
    default <T extends Enum<T>> @Nullable T getEnum(@NotNull List<String> path, @NotNull Class<T> enumType) {
        return getEnum(path, enumType, EnumGetMethod.NAME_IGNORECASE);
    }

    /**
     * Gets an optional Enum value from the config.
     *
     * @param path     the value's path, each part separated by a dot. Example "a.b.c"
     * @param enumType the class of the Enum
     * @param method   the method to use when converting a non-enum value like a String or an int
     * @param <T>      the value's type
     * @return the value at the given path as an enum, or {@code null} if not found.
     * @throws IllegalArgumentException if the config contains a String that doesn't match any of
     *                                  the enum constants, with regards to the given method
     * @throws ClassCastException       if the config contains a value that cannot be converted to
     *                                  an enum constant, like a List
     */
    default <T extends Enum<T>> @NotNull Optional<T> getOptionalEnum(@NotNull String path, @NotNull Class<T> enumType, @NotNull EnumGetMethod method) {
        return getOptionalEnum(split(path, '.'), enumType, method);
    }

    /**
     * Calls {@link #getOptionalEnum(String, Class, EnumGetMethod)} with method
     * {@link EnumGetMethod#NAME_IGNORECASE}.
     *
     * @param path     the value's path, each part separated by a dot. Example "a.b.c"
     * @param enumType the class of the Enum
     * @return the value at the given path as an enum, or {@code null} if not found.
     */
    default <T extends Enum<T>> @NotNull Optional<T> getOptionalEnum(@NotNull String path, @NotNull Class<T> enumType) {
        return getOptionalEnum(path, enumType, EnumGetMethod.NAME_IGNORECASE);
    }

    /**
     * Gets an optional Enum value from the config.
     *
     * @param path     the value's path, each element of the list is a different part of the path.
     * @param enumType the class of the Enum
     * @param method   the method to use when converting a non-enum value like a String or an int
     * @param <T>      the value's type
     * @return the value at the given path as an enum, or {@code null} if not found.
     * @throws IllegalArgumentException if the config contains a String that doesn't match any of
     *                                  the enum constants, with regards to the given method
     * @throws ClassCastException       if the config contains a value that cannot be converted to
     *                                  an enum constant, like a List
     */
    default <T extends Enum<T>> @NotNull Optional<T> getOptionalEnum(@NotNull List<String> path, @NotNull Class<T> enumType, @NotNull EnumGetMethod method) {
        return Optional.ofNullable(getEnum(path, enumType, method));
    }

    /**
     * Calls {@link #getOptionalEnum(List, Class, EnumGetMethod)} with method
     * {@link EnumGetMethod#NAME_IGNORECASE}.
     *
     * @param path     the value's path, each element of the list is a different part of the path.
     * @param enumType the class of the Enum
     * @return the value at the given path as an enum, or {@code null} if not found.
     */
    default <T extends Enum<T>> @NotNull Optional<T> getOptionalEnum(@NotNull List<String> path, @NotNull Class<T> enumType) {
        return getOptionalEnum(path, enumType, EnumGetMethod.NAME_IGNORECASE);
    }

    /**
     * Gets an Enum value from the config. If the value doesn't exist, returns the default value.
     *
     * @param path         the value's path, each part separated by a dot. Example "a.b.c"
     * @param defaultValue the default value
     * @param method       the method to use when converting a non-enum value like a String or an int
     * @param <T>          the value's type
     * @return the value at the given path as an enum, or {@code null} if not found.
     * @throws IllegalArgumentException if the config contains a String that doesn't match any of
     *                                  the enum constants, with regards to the given method
     * @throws ClassCastException       if the config contains a value that cannot be converted to
     *                                  an enum constant, like a List
     */
    default <T extends Enum<T>> @NotNull T getEnumOrElse(@NotNull String path, @NotNull T defaultValue, @NotNull EnumGetMethod method) {
        return getEnumOrElse(split(path, '.'), defaultValue, method);
    }

    /**
     * Calls {@link #getEnumOrElse(String, Enum, EnumGetMethod)} with method
     * {@link EnumGetMethod#NAME_IGNORECASE}.
     *
     * @param path         the value's path, each part separated by a dot. Example "a.b.c"
     * @param defaultValue the default value
     * @return the value at the given path as an enum, or {@code null} if not found.
     */
    default <T extends Enum<T>> @NotNull T getEnumOrElse(@NotNull String path, @NotNull T defaultValue) {
        return getEnumOrElse(path, defaultValue, EnumGetMethod.NAME_IGNORECASE);
    }

    /**
     * Gets an Enum value from the config. If the value doesn't exist, returns the default value.
     *
     * @param path         the value's path, each element of the list is a different part of the path.
     * @param defaultValue the default value
     * @param method       the method to use when converting a non-enum value like a String or an int
     * @param <T>          the value's type
     * @return the value at the given path as an enum, or {@code null} if not found.
     * @throws IllegalArgumentException if the config contains a String that doesn't match any of
     *                                  the enum constants, with regards to the given method
     * @throws ClassCastException       if the config contains a value that cannot be converted to
     *                                  an enum constant, like a List
     */
    default <T extends Enum<T>> @NotNull T getEnumOrElse(@NotNull List<String> path, @NotNull T defaultValue, @NotNull EnumGetMethod method) {
        T value = getEnum(path, defaultValue.getDeclaringClass(), method);
        return (value == null) ? defaultValue : value;
    }

    /**
     * Calls {@link #getEnumOrElse(List, Enum, EnumGetMethod)} with method
     * {@link EnumGetMethod#NAME_IGNORECASE}.
     *
     * @param path         the value's path, each element of the list is a different part of the path.
     * @param defaultValue the default value
     * @return the value at the given path as an enum, or {@code null} if not found.
     */
    default <T extends Enum<T>> @NotNull T getEnumOrElse(@NotNull List<String> path, @NotNull T defaultValue) {
        return getEnumOrElse(path, defaultValue, EnumGetMethod.NAME_IGNORECASE);
    }

    /**
     * Gets an Enum value from the config. If the value doesn't exist, returns the default value.
     *
     * @param path                 the value's path, each element of the list is a different part of the path.
     * @param defaultValueSupplier Supplier of the default value, only used if needed
     * @param method               the method to use when converting a non-enum value like a String or an int
     * @param <T>                  the value's type
     * @return the value at the given path as an enum, or {@code null} if not found.
     * @throws IllegalArgumentException if the config contains a String that doesn't match any of
     *                                  the enum constants, with regards to the given method
     * @throws ClassCastException       if the config contains a value that cannot be converted to
     *                                  an enum constant, like a List
     */
    default <T extends Enum<T>> T getEnumOrElse(@NotNull String path, @NotNull Class<T> enumType, @NotNull EnumGetMethod method, @NotNull Supplier<T> defaultValueSupplier) {
        return getEnumOrElse(split(path, '.'), enumType, method, defaultValueSupplier);
    }

    /**
     * Calls {@link #getEnumOrElse(String, Class, EnumGetMethod, Supplier)} with method
     * {@link EnumGetMethod#NAME_IGNORECASE}.
     *
     * @param path     the path to check, each part separated by a dot. Example "a.b.c"
     * @param enumType the class of the Enum
     * @return the value at the given path as an enum, or {@code null} if not found.
     */
    default <T extends Enum<T>> T getEnumOrElse(@NotNull String path, @NotNull Class<T> enumType, @NotNull Supplier<T> defaultValueSupplier) {
        return getEnumOrElse(path, enumType, EnumGetMethod.NAME_IGNORECASE, defaultValueSupplier);
    }

    /**
     * Gets an Enum value from the config. If the value doesn't exist, returns the default value.
     *
     * @param path                 the value's path, each element of the list is a different part of the path.
     * @param defaultValueSupplier Supplier of the default value, only used if needed
     * @param method               the method to use when converting a non-enum value like a String or an int
     * @param <T>                  the value's type
     * @return the value at the given path as an enum, or {@code null} if not found.
     * @throws IllegalArgumentException if the config contains a String that doesn't match any of
     *                                  the enum constants, with regards to the given method
     * @throws ClassCastException       if the config contains a value that cannot be converted to
     *                                  an enum constant, like a List
     */
    default <T extends Enum<T>> T getEnumOrElse(@NotNull List<String> path, @NotNull Class<T> enumType, @NotNull EnumGetMethod method, @NotNull Supplier<T> defaultValueSupplier) {
        // The enumType is needed to avoid using the Supplier when the raw value is an enum constant
        T value = getEnum(path, enumType, method);
        return (value == null) ? defaultValueSupplier.get() : value;
    }

    /**
     * Calls {@link #getEnumOrElse(List, Class, EnumGetMethod, Supplier)} with method
     * {@link EnumGetMethod#NAME_IGNORECASE}.
     *
     * @param path                 the value's path, each element of the list is a different part of the path.
     * @param enumType             the class of the Enum
     * @param defaultValueSupplier Supplier of the default value, only used if needed
     * @return the value at the given path as an enum, or {@code null} if not found.
     */
    default <T extends Enum<T>> T getEnumOrElse(@NotNull List<String> path, @NotNull Class<T> enumType, @NotNull Supplier<T> defaultValueSupplier) {
        return getEnumOrElse(path, enumType, EnumGetMethod.NAME_IGNORECASE, defaultValueSupplier);
    }

    // ---- Primitive getters: int ----

    /**
     * Like {@link #get(String)} but returns a primitive int. The config's value must be a
     * {@link Number}. Throws an exception if the value does not exist.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     * @return the value at the given path, as {@link Number#intValue()}.
     */
    default int getInt(@NotNull String path) {
        return this.<Number>get(path).intValue();
    }

    /**
     * Like {@link #get(List)} but returns a primitive int. The config's value must be a
     * {@link Number}. Throws an exception if the value does not exist.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @return the value at the given path, as {@link Number#intValue()}.
     */
    default int getInt(@NotNull List<String> path) {
        return this.<Number>getRaw(path).intValue();
    }

    /**
     * Like {@link #getOptional(String)} but returns a primitive int. The config's value must be a
     * {@link Number} or null or nonexistant.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     * @return the value at the given path, as {@link Number#intValue()}, or {@link OptionalInt#empty()}.
     */
    default @NotNull OptionalInt getOptionalInt(@NotNull String path) {
        return getOptionalInt(split(path, '.'));
    }

    /**
     * Like {@link #getOptional(List)} but returns a primitive int. The config's value must be a
     * {@link Number} or null or nonexistant.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @return the value at the given path, as {@link Number#intValue()}, or {@link OptionalInt#empty()}.
     */
    default @NotNull OptionalInt getOptionalInt(@NotNull List<String> path) {
        Number n = get(path);
        return (n == null) ? OptionalInt.empty() : OptionalInt.of(n.intValue());
    }

    /**
     * Like {@link #getOrElse(String, Object)} but returns a primitive int.
     * The config's value must be a {@link Number} or null or nonexistant.
     *
     * @param path         the path to check, each part separated by a dot. Example "a.b.c"
     * @param defaultValue the value returned if the config doesn't contain the given path
     * @return the value at the given path, as {@link Number#intValue()}, or {@code defaultValue}.
     */
    default int getIntOrElse(@NotNull String path, int defaultValue) {
        return getIntOrElse(split(path, '.'), defaultValue);
    }

    /**
     * Like {@link #getOrElse(List, Object)} but returns a primitive int.
     * The config's value must be a {@link Number} or null or nonexistant.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @return the value at the given path, as {@link Number#intValue()}, or {@code defaultValue}.
     */
    default int getIntOrElse(@NotNull List<String> path, int defaultValue) {
        Number n = get(path);
        return (n == null) ? defaultValue : n.intValue();
    }

    /**
     * Like {@link #getOrElse(String, Supplier)} but returns a primitive int.
     * The config's value must be a {@link Number} or null or nonexistant.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     * @return the value at the given path, as {@link Number#intValue()}, or {@code defaultValueSupplier.get()}.
     */
    default int getIntOrElse(@NotNull String path, @NotNull IntSupplier defaultValueSupplier) {
        return getIntOrElse(split(path, '.'), defaultValueSupplier);
    }

    /**
     * Like {@link #getOrElse(List, Supplier)} but returns a primitive int.
     * The config's value must be a {@link Number} or null or nonexistant.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @return the value at the given path, as {@link Number#intValue()}, or {@code defaultValueSupplier.get()}.
     */
    default int getIntOrElse(@NotNull List<String> path, @NotNull IntSupplier defaultValueSupplier) {
        Number n = get(path);
        return (n == null) ? defaultValueSupplier.getAsInt() : n.intValue();
    }

    // ---- Primitive getters: double ----

    /**
     * Like {@link #get(String)} but returns a primitive double. The config's value must be a
     * {@link Number}. Throws an exception if the value does not exist.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     * @return the value at the given path, as {@link Number#doubleValue()}.
     */
    default double getDouble(@NotNull String path) {
        return this.<Number>getRaw(path).doubleValue();
    }

    /**
     * Like {@link #get(List)} but returns a primitive double. The config's value must be a
     * {@link Number}. Throws an exception if the value does not exist.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @return the value at the given path, as {@link Number#doubleValue()}.
     */
    default double getDouble(@NotNull List<String> path) {
        return this.<Number>getRaw(path).doubleValue();
    }

    /**
     * Like {@link #getOptional(String)} but returns a primitive double. The config's value must be a
     * {@link Number} or null or nonexistant.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     * @return the value at the given path, as {@link Number#doubleValue()}, or {@link OptionalDouble#empty()}.
     */
    default @NotNull OptionalDouble getOptionalDouble(@NotNull String path) {
        return getOptionalDouble(split(path, '.'));
    }

    /**
     * Like {@link #getOptional(List)} but returns a primitive double. The config's value must be a
     * {@link Number} or null or nonexistant.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @return the value at the given path, as {@link Number#doubleValue()}, or {@link OptionalDouble#empty()}.
     */
    default @NotNull OptionalDouble getOptionalDouble(@NotNull List<String> path) {
        Number n = get(path);
        return (n == null) ? OptionalDouble.empty() : OptionalDouble.of(n.doubleValue());
    }

    /**
     * Like {@link #getOrElse(String, Object)} but returns a primitive double.
     * The config's value must be a {@link Number} or null or nonexistant.
     *
     * @param path         the path to check, each part separated by a dot. Example "a.b.c"
     * @param defaultValue the value returned if the config doesn't contain the given path
     * @return the value at the given path, as {@link Number#doubleValue()}, or {@code defaultValue}.
     */
    default double getDoubleOrElse(@NotNull String path, double defaultValue) {
        return getDoubleOrElse(split(path, '.'), defaultValue);
    }

    /**
     * Like {@link #getOrElse(List, Object)} but returns a primitive double.
     * The config's value must be a {@link Number} or null or nonexistant.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @return the value at the given path, as {@link Number#doubleValue()}, or {@code defaultValue}.
     */
    default double getDoubleOrElse(@NotNull List<String> path, double defaultValue) {
        Number n = get(path);
        return (n == null) ? defaultValue : n.doubleValue();
    }

    /**
     * Like {@link #getOrElse(String, Supplier)} but returns a primitive double.
     * The config's value must be a {@link Number} or null or nonexistant.
     *
     * @param path                 the path to check, each part separated by a dot. Example "a.b.c"
     * @param defaultValueSupplier supplies the value to return if the config doesn't contain the path
     */
    default double getDoubleOrElse(@NotNull String path, @NotNull DoubleSupplier defaultValueSupplier) {
        return getDoubleOrElse(split(path, '.'), defaultValueSupplier);
    }

    /**
     * Like {@link #getOrElse(List, Supplier)} but returns a primitive double.
     * The config's value must be a {@link Number} or null or nonexistant.
     *
     * @param path                 the value's path, each element of the list is a different part of the path.
     * @param defaultValueSupplier supplies the value to return if the config doesn't contain the path
     */
    default double getDoubleOrElse(@NotNull List<String> path, @NotNull DoubleSupplier defaultValueSupplier) {
        Number n = get(path);
        return (n == null) ? defaultValueSupplier.getAsDouble() : n.doubleValue();
    }

    // ---- Primitive getters: float ----

    /**
     * Like {@link #get(String)} but returns a primitive float. The config's value must be a
     * {@link Number}. Throws an exception if the value does not exist.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     * @return the value at the given path, as {@link Number#floatValue()}.
     */
    default float getFloat(@NotNull String path) {
        return this.<Number>getRaw(path).floatValue();
    }

    /**
     * Like {@link #get(List)} but returns a primitive float. The config's value must be a
     * {@link Number}. Throws an exception if the value does not exist.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @return the value at the given path, as {@link Number#floatValue()}.
     */
    default float getFloat(@NotNull List<String> path) {
        return this.<Number>getRaw(path).floatValue();
    }

    /**
     * Like {@link #getOptional(String)} but returns a primitive float. The config's value must be a
     * {@link Number} or null or nonexistant.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     * @return the value at the given path, as {@link Number#floatValue()}, or {@link OptionalDouble#empty()}.
     */
    default float getFloatOrElse(@NotNull String path, float defaultValue) {
        return getFloatOrElse(split(path, '.'), defaultValue);
    }

    /**
     * Like {@link #getOptional(List)} but returns a primitive float. The config's value must be a
     * {@link Number} or null or nonexistant.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @return the value at the given path, as {@link Number#floatValue()}, or {@link OptionalDouble#empty()}.
     */
    default float getFloatOrElse(@NotNull List<String> path, float defaultValue) {
        Number n = get(path);
        return (n == null) ? defaultValue : n.floatValue();
    }

    /**
     * Like {@link #getOrElse(String, Supplier)} but returns a primitive float.
     * The config's value must be a {@link Number} or null or nonexistant.
     *
     * @param path                 the path to check, each part separated by a dot. Example "a.b.c"
     * @param defaultValueSupplier supplies the value to return if the config doesn't contain the path
     */
    default float getFloatOrElse(@NotNull String path, @NotNull Supplier<Float> defaultValueSupplier) {
        return getFloatOrElse(split(path, '.'), defaultValueSupplier);
    }

    /**
     * Like {@link #getOrElse(List, Supplier)} but returns a primitive float.
     * The config's value must be a {@link Number} or null or nonexistant.
     *
     * @param path                 the value's path, each element of the list is a different part of the path.
     * @param defaultValueSupplier supplies the value to return if the config doesn't contain the path
     */
    default float getFloatOrElse(@NotNull List<String> path, @NotNull Supplier<Float> defaultValueSupplier) {
        Number n = get(path);
        return (n == null) ? defaultValueSupplier.get() : n.floatValue();
    }

    // ---- Primitive getters: long ----

    /**
     * Like {@link #get(String)} but returns a primitive long. The config's value must be a
     * {@link Number}.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     */
    default long getLong(@NotNull String path) {
        return this.<Number>getRaw(path).longValue();
    }

    /**
     * Like {@link #get(List)} but returns a primitive long. The config's value must be a
     * {@link Number}.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     */
    default long getLong(@NotNull List<String> path) {
        return this.<Number>getRaw(path).longValue();
    }

    /**
     * Like {@link #getOptional(String)} but returns a primitive long. The config's value must be a
     * {@link Number} or null or nonexistant.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     */
    default @NotNull OptionalLong getOptionalLong(@NotNull String path) {
        return getOptionalLong(split(path, '.'));
    }

    /**
     * Like {@link #getOptional(List)} but returns a primitive long. The config's value must be a
     * {@link Number} or null or nonexistant.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     */
    default @NotNull OptionalLong getOptionalLong(@NotNull List<String> path) {
        Number n = get(path);
        return (n == null) ? OptionalLong.empty() : OptionalLong.of(n.longValue());
    }

    /**
     * Like {@link #getOrElse(String, Object)} but returns a primitive long.
     * The config's value must be a {@link Number} or null or nonexistant.
     *
     * @param path         the path to check, each part separated by a dot. Example "a.b.c"
     * @param defaultValue the value to return if the config doesn't contain the path
     */
    default long getLongOrElse(@NotNull String path, long defaultValue) {
        return getLongOrElse(split(path, '.'), defaultValue);
    }

    /**
     * Like {@link #getOrElse(List, Object)} but returns a primitive long.
     * The config's value must be a {@link Number} or null or nonexistant.
     *
     * @param path         the value's path, each element of the list is a different part of the path.
     * @param defaultValue the value to return if the config doesn't contain the path
     */
    default long getLongOrElse(@NotNull List<String> path, long defaultValue) {
        Number n = get(path);
        return (n == null) ? defaultValue : n.longValue();
    }

    /**
     * Like {@link #getOrElse(String, Supplier)} but returns a primitive long.
     * The config's value must be a {@link Number} or null or nonexistant.
     *
     * @param path                 the path to check, each part separated by a dot. Example "a.b.c"
     * @param defaultValueSupplier supplies the value to return if the config doesn't contain the path
     */
    default long getLongOrElse(@NotNull String path, @NotNull LongSupplier defaultValueSupplier) {
        return getLongOrElse(split(path, '.'), defaultValueSupplier);
    }

    /**
     * Like {@link #getOrElse(List, Supplier)} but returns a primitive long.
     * The config's value must be a {@link Number} or null or nonexistant.
     *
     * @param path                 the value's path, each element of the list is a different part of the path.
     * @param defaultValueSupplier supplies the value to return if the config doesn't contain the path
     */
    default long getLongOrElse(@NotNull List<String> path, @NotNull LongSupplier defaultValueSupplier) {
        Number n = get(path);
        return (n == null) ? defaultValueSupplier.getAsLong() : n.longValue();
    }

    // ---- Primitive getters: byte ----
    default byte getByte(@NotNull String path) {
        return this.<Number>getRaw(path).byteValue();
    }

    default byte getByte(@NotNull List<String> path) {
        return this.<Number>getRaw(path).byteValue();
    }

    default byte getByteOrElse(@NotNull String path, byte defaultValue) {
        return getByteOrElse(split(path, '.'), defaultValue);
    }

    default byte getByteOrElse(@NotNull List<String> path, byte defaultValue) {
        Number n = get(path);
        return (n == null) ? defaultValue : n.byteValue();
    }

    // ---- Primitive getters: short ----
    default short getShort(@NotNull String path) {
        return this.<Number>getRaw(path).shortValue();
    }

    default short getShort(@NotNull List<String> path) {
        return this.<Number>getRaw(path).shortValue();
    }

    default short getShortOrElse(@NotNull String path, short defaultValue) {
        return getShortOrElse(split(path, '.'), defaultValue);
    }

    default short getShortOrElse(@NotNull List<String> path, short defaultValue) {
        Number n = get(path);
        return (n == null) ? defaultValue : n.shortValue();
    }

    // ---- Primitive getters: char ----

    /**
     * Returns a char value from the configuration.
     * <p>
     * If the value is a Number, returns {@link Number#intValue()}, cast to char.
     * If the value is a CharSequence, returns its first character.
     * Otherwise, attempts to cast the value to a char.
     *
     * @param path the value's path as a dot-separated String
     * @return the value, as a single char
     */
    default char getChar(@NotNull String path) {
        return (char) getInt(path);
    }

    /**
     * Returns a char value from the configuration.
     * <p>
     * If the value is a Number, returns {@link Number#intValue()}, cast to char.
     * If the value is a CharSequence, returns its first character.
     * Otherwise, attempts to cast the value to a char.
     *
     * @param path the value's path as a list of String
     * @return the value, as a single char
     */
    default char getChar(@NotNull List<String> path) {
        Object value = getRaw(path);
        if (value instanceof Number) {
            return (char) ((Number) value).intValue();
        } else if (value instanceof CharSequence) {
            return ((CharSequence) value).charAt(0);
        } else {
            return (char) Objects.requireNonNull(value);
        }
    }

    /**
     * Returns a char value from the configuration.
     * <p>
     * If the value is nonexistant, returns defaultValue.
     * If the value is a Number, returns {@link Number#intValue()}, cast to char.
     * If the value is a CharSequence, returns its first character.
     * Otherwise, attempts to cast the value to a char.
     *
     * @param path         the value's path
     * @param defaultValue the char to return if the value doesn't exist in the config
     * @return the value, as a single char
     */
    default char getCharOrElse(@NotNull String path, char defaultValue) {
        return getCharOrElse(split(path, '.'), defaultValue);
    }

    /**
     * Returns a char value from the configuration.
     * <p>
     * If the value is nonexistant, returns defaultValue.
     * If the value is a Number, returns {@link Number#intValue()}, cast to char.
     * If the value is a CharSequence, returns its first character.
     * Otherwise, attempts to cast the value to a char.
     *
     * @param path         the value's path
     * @param defaultValue the char to return if the value doesn't exist in the config
     * @return the value, as a single char
     */
    default char getCharOrElse(@NotNull List<String> path, char defaultValue) {
        Object value = getRaw(path);
        if (value == null || value == NULL_OBJECT) {
            return defaultValue;
        } else if (value instanceof Number) {
            return (char) ((Number) value).intValue();
        } else if (value instanceof CharSequence) {
            return ((CharSequence) value).charAt(0);
        } else {
            return (char) value;
        }
    }
    // ---- End of getters ----


    /**
     * Checks if the config contains a value at some path.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     * @return {@code true} if the path is associated with a value, {@code false} if it's not.
     */
    default boolean contains(@NotNull String path) {
        return contains(split(path, '.'));
    }

    /**
     * Checks if the config contains a value at some path.
     *
     * @param path the path to check, each element of the list is a different part of the path.
     * @return {@code true} if the path is associated with a value, {@code false} if it's not.
     */
    boolean contains(@NotNull List<String> path);

    /**
     * Checks if the config contains a null value at some path.
     *
     * @param path the path to check, each part separated by a dot. Example "a.b.c"
     * @return {@code true} if the path is associated with {@link NullObject#NULL_OBJECT},
     * {@code false} if it's associated with another value or with no value.
     */
    default boolean isNull(@NotNull String path) {
        return isNull(split(path, '.'));
    }

    /**
     * Checks if the config contains a null value at some path.
     *
     * @param path the path to check, each element of the list is a different part of the path.
     * @return {@code true} if the path is associated with {@link NullObject#NULL_OBJECT},
     * {@code false} if it's associated with another value or with no value.
     */
    default boolean isNull(@NotNull List<String> path) {
        return getRaw(path) == NULL_OBJECT;
    }

    /**
     * Gets the size of the config.
     *
     * @return the number of top-level elements in the config.
     */
    int size();

    /**
     * Checks if the config is empty.
     *
     * @return {@code true} if the config is empty, {@code false} if it contains at least one
     * element.
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns a Map view of the config's values. If the config is unmodifiable then the returned
     * map is unmodifiable too.
     *
     * @return a Map view of the config's values.
     * @deprecated valueMap() may not work exactly as a regular Map for some config types, in
     * particular {@link ConcurrentConfig}, and may be removed in a future version.
     * Prefer to use {@link #entrySet()} instead.
     */
    @Deprecated
    Map<String, Object> valueMap();

    /**
     * Returns a Set view of the config's entries. If the config is unmodifiable then the returned
     * set is unmodifiable too.
     *
     * @return a Set view of the config's entries.
     */
    Set<? extends Entry> entrySet();

    /**
     * An unmodifiable config entry.
     */
    interface Entry {
        /**
         * @return the entry's key
         */
        String getKey();

        /**
         * Returns the entry's value without converting {@link NullObject#NULL_OBJECT} to {@code null}.
         *
         * @param <T> the value's type
         * @return the entry's value
         */
        <T> T getRawValue();

        /**
         * @param <T> the value's type
         * @return the entry's value
         */
        @SuppressWarnings("unchecked")
        default <T> @Nullable T getValue() {
            Object raw = getRawValue();
            return (raw == NULL_OBJECT) ? null : (T) raw;
        }

        /**
         * @return {@code true} if the value is {@link NullObject#NULL_OBJECT}.
         */
        default boolean isNull() {
            return getRawValue() == NULL_OBJECT;
        }

        /**
         * @param <T> the value's type
         * @return the entry's value, wrapped in {@link Optional}
         */
        default <T> @NotNull Optional<T> getOptional() {
            return Optional.ofNullable(getValue());
        }

        default <T> T getOrElse(T defaultValue) {
            T value = getRawValue();
            return (value == null || value == NULL_OBJECT) ? defaultValue : value;
        }

        // ---- Primitive getters: int ----

        /**
         * @return the entry's value as an int
         */
        default int getInt() {
            return this.<Number>getRawValue().intValue();
        }

        default @NotNull OptionalInt getOptionalInt() {
            Number value = getRawValue();
            return (value == null) ? OptionalInt.empty() : OptionalInt.of(value.intValue());
        }

        default int getIntOrElse(int defaultValue) {
            Number value = getRawValue();
            return (value == null) ? defaultValue : value.intValue();
        }

        // ---- Primitive getters: long ----

        /**
         * @return the entry's value as a long
         */
        default long getLong() {
            return this.<Number>getRawValue().longValue();
        }

        default @NotNull OptionalLong getOptionalLong() {
            Number value = getRawValue();
            return (value == null) ? OptionalLong.empty() : OptionalLong.of(value.longValue());
        }

        default long getLongOrElse(long defaultValue) {
            Number value = getRawValue();
            return (value == null) ? defaultValue : value.longValue();
        }

        // ---- Primitive getters: byte ----

        /**
         * @return the entry's value as a byte
         */
        default byte getByte() {
            return this.<Number>getRawValue().byteValue();
        }

        default byte getByteOrElse(byte defaultValue) {
            Number value = getRawValue();
            return (value == null) ? defaultValue : value.byteValue();
        }

        /**
         * @return the entry's value as a short
         */
        default short getShort() {
            return this.<Number>getRawValue().shortValue();
        }

        default short getShortOrElse(short defaultValue) {
            Number value = getRawValue();
            return (value == null) ? defaultValue : value.shortValue();
        }

        // ---- Primitive getters: char ----

        /**
         * If the value is a Number, returns {@link Number#intValue()}, cast to char.
         * If the value is a CharSequence, returns its first character.
         * Otherwise, attempts to cast the value to a char.
         *
         * @return the entry's value as a char
         */
        default char getChar() {
            Object value = getRawValue();
            if (value instanceof Number) {
                return (char) ((Number) value).intValue();
            } else if (value instanceof CharSequence) {
                return ((CharSequence) value).charAt(0);
            } else {
                return (char) value;
            }
        }

        default char getCharOrElse(char defaultValue) {
            Object value = getRawValue();
            return switch (value) {
                case null -> defaultValue;
                case Number number -> (char) number.intValue();
                case CharSequence charSequence -> charSequence.charAt(0);
                default -> (char) value;
            };
        }
    }

    /**
     * Returns the config's format.
     *
     * @return the config's format
     */
    ConfigFormat<?> configFormat();

    //--- Scala convenience methods ---

    /**
     * For scala: gets a config value.
     *
     * @param path the value's path, each part separated by a dot. Example "a.b.c"
     * @param <T>  the value's type
     * @see #get(String)
     */
    default <T> @Nullable T apply(@NotNull String path) {
        return get(path);
    }

    /**
     * For scala: gets a config value.
     *
     * @param path the value's path, each element of the list is a different part of the path.
     * @param <T>  the value's type
     * @see #get(List)
     */
    default <T> @Nullable T apply(@NotNull List<String> path) {
        return get(path);
    }
}
