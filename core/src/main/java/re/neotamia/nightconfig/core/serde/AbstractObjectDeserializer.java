package re.neotamia.nightconfig.core.serde;

import java.util.*;
import java.util.function.Supplier;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.jetbrains.annotations.NotNull;
import re.neotamia.nightconfig.core.NullObject;
import re.neotamia.nightconfig.core.UnmodifiableConfig;
import re.neotamia.nightconfig.core.serde.annotations.SerdeDefault;
import re.neotamia.nightconfig.core.serde.annotations.SerdePhase;

/**
 * AbstractObjectDeserializer, common to all Java versions.
 */
class AbstractObjectDeserializer {
	protected final List<ValueDeserializerProvider<?, ?>> generalProviders;
	protected ValueDeserializerProvider<?, ?> defaultProvider;
	protected final boolean applyTransientModifier;
	protected NamingStrategy namingStrategy;

	protected AbstractObjectDeserializer(ObjectDeserializerBuilder builder) {
		this.generalProviders = builder.deserializerProviders;
		this.defaultProvider = Objects.requireNonNull(builder.defaultProvider);
		this.applyTransientModifier = builder.applyTransientModifier;
		this.namingStrategy = builder.namingStrategy;
	}

	// NOTE: it would make no sense to provide a method deserialize(Object) ->
	// Object, because
	// the trivial deserialization can always be applied when there is no constraint
	// on the result.
	// ObjectSerializer.serialize does exist, however, because there is a constraint
	// on the type of
	// values that the configuration can contain.

	/**
	 * Deserializes a configuration value into an instance of the collection
	 * {@code C<V>}.
	 *
	 * @param <C>             type of the collection
	 * @param <V>             type of the values in the collection
	 * @param configValue     config value to deserialize
	 * @param collectionClass class of the collection
	 * @param valueClass      class of the values in the collection
	 * @return the deserialized collection
	 */
	@SuppressWarnings("unchecked")
	protected <C extends Collection<V>, V> C deserializeToCollection(Object configValue, Class<C> collectionClass, Class<V> valueClass) {
		DeserializerContext ctx = new DeserializerContext(this);
		TypeConstraint t = new TypeConstraint(new TypeConstraint.ManuallyParameterized(collectionClass, valueClass));
		return (C) ctx.deserializeValue(configValue, t);
	}

	/**
	 * Deserializes a configuration value into an instance of the map
	 * {@code M<String, V>}.
	 *
	 * @param <M>         type of the map
	 * @param <V>         type of the values in the map
	 * @param configValue config value to deserialize
	 * @param mapClass    class of the map
	 * @param valueClass  class of the values in the collection
	 * @return the deserialized map
	 */
	@SuppressWarnings("unchecked")
	protected <M extends Map<String, V>, V> M deserializeToMap(Object configValue, Class<M> mapClass, Class<V> valueClass) {
		DeserializerContext ctx = new DeserializerContext(this);
		TypeConstraint t = new TypeConstraint(new TypeConstraint.ManuallyParameterized(mapClass, String.class, valueClass));
		return (M) ctx.deserializeValue(configValue, t);
	}

	/**
	 * Deserializes a {@code Config} as an object by transforming its entries into
	 * fields.
	 * The fields of the {@code destination} are modified through reflection.
	 *
	 * @param source      config to deserialize
	 * @param destination object to store the result in
	 */
	protected void deserializeFields(UnmodifiableConfig source, Object destination) {
		DeserializerContext ctx = new DeserializerContext(this);
		ctx.deserializeFields(source, destination);
	}

	/**
	 * Deserializes a {@code Config} as an object of type {@code R} by transforming
	 * its entries
	 * into fields. A new instance of the object is created, and its fields are
	 * modified through reflection.
	 *
	 * @param <R>                 type of the resulting object
	 * @param source              config to deserialize
	 * @param destinationSupplier supplier of the resulting object
	 * @return the deserialized object
	 */
	protected <R> R deserializeFields(UnmodifiableConfig source, Supplier<? extends R> destinationSupplier) {
		R dest = destinationSupplier.get();
		deserializeFields(source, dest);
		return dest;
	}

	@SuppressWarnings("unchecked")
	protected <T, R> ValueDeserializer<T, R> findValueDeserializer(T value, TypeConstraint resultType) {
		Type valueType = value == null ? null : value.getClass();
		ValueDeserializer<?, ?> maybeDe;
		for (ValueDeserializerProvider<?, ?> provider : generalProviders) {
			maybeDe = provider.provide(valueType, resultType);
			if (maybeDe != null) {
				return (ValueDeserializer<T, R>) maybeDe;
			}
		}
		maybeDe = defaultProvider.provide(valueType, resultType);
		if (maybeDe != null) {
			return (ValueDeserializer<T, R>) maybeDe;
		}
		Class<?> valueClass = (valueType instanceof Class) ? (Class<?>)valueType : null;
		String ofTypeStr = valueClass == null ? "" : " of type " + valueClass;
		throw new SerdeException("No suitable deserializer found for value" + ofTypeStr + ": "+ value + " and result constraint " + resultType);
	}

	protected Supplier<?> findDefaultValueSupplier(Object rawConfigValue, Field field, Object instance) {
		// Start with standalone SerdeDefault annotations
        EnumMap<SerdePhase, EnumMap<SerdeDefault.WhenValue, SerdeDefault>> defaultAnnotations = AnnotationProcessor.createSerdePhaseEnumMapEnumMap(field);
        EnumMap<SerdeDefault.WhenValue, SerdeDefault> defaultForDeserializing = defaultAnnotations.get(SerdePhase.DESERIALIZING);

		if (defaultForDeserializing == null) {
			return null; // no default
		}

		SerdeDefault applicableDefault = null;
		if (rawConfigValue == null) {
			// missing value
			applicableDefault = defaultForDeserializing.get(SerdeDefault.WhenValue.IS_MISSING);
		} else if (rawConfigValue == NullObject.NULL_OBJECT) {
			// null value
			applicableDefault = defaultForDeserializing.get(SerdeDefault.WhenValue.IS_NULL);
		} else {
			// avoid to call Util.isEmpty() if there's no default for empty values
			SerdeDefault forEmpty = defaultForDeserializing.get(SerdeDefault.WhenValue.IS_EMPTY);
			if (forEmpty != null && Util.isEmpty(rawConfigValue)) {
				applicableDefault = forEmpty;
			}
		}

		if (applicableDefault == null) {
			return null; // no applicable default for our config value
		}

		return AnnotationProcessor.resolveConfigDefaultProvider(applicableDefault, instance);
	}

	/**
	 * Adds a {@link ValueDeserializer} that will be used to deserialize config values
	 * of type {@code valueClass} to objects of type {@code resultClass}.
	 *
	 * @param <V>          type of the config values to deserialize
	 * @param <R>          resulting type of the deserialization
	 * @param valueClass   class of the config values to deserialize
	 * @param resultClass  class of the deserialization result
	 * @param deserializer deserializer to register
	 */
	protected <V, R> void registerDeserializerForClass(Class<V> valueClass, Class<R> resultClass, ValueDeserializer<? super V, ? extends R> deserializer) {
		registerDeserializerProvider((valueType, resultTypeConstraint) -> resultTypeConstraint.getSatisfyingRawType().map(resultCls -> {
			Class<?> valueCls = new TypeConstraint(valueType).getSatisfyingRawType().orElse(null);
			if (valueCls != null && Util.canAssign(valueClass, valueCls) && resultCls.isAssignableFrom(resultClass))
				return deserializer;
			return null;
		}).orElse(null));
	}

	/**
	 * Adds a {@link ValueDeserializer} that will be used to deserialize config values
	 * of a specific type to a specific result type.
	 *
	 * @param <V>          type of the config values to deserialize
	 * @param <R>          resulting type of the deserialization
	 * @param valueClass   class of the config values to deserialize
	 * @param resultType   type of the deserialization result
	 * @param deserializer deserializer to register
	 */
	protected <V, R> void registerDeserializerForType(Class<V> valueClass, Type resultType, ValueDeserializer<? super V, ? extends R> deserializer) {
		registerDeserializerProvider((valueType, resultTypeConstraint) -> {
			Class<?> valueCls = new TypeConstraint(valueType).getSatisfyingRawType().orElse(null);
			if (valueCls != null && Util.canAssign(valueClass, valueCls) && resultTypeConstraint.getFullType().equals(resultType))
				return deserializer;
			return null;
		});
	}

	/**
	 * Adds a {@link ValueDeserializerProvider} that provides {@link ValueDeserializer} to
	 * deserialize config values.
	 *
	 * @param <V>      type of the config values to deserialize
	 * @param <R>      resulting type of the deserialization
	 * @param provider provider to register
	 */
	protected <V, R> void registerDeserializerProvider(ValueDeserializerProvider<V, R> provider) {
		generalProviders.add(provider);
	}

    /**
     * Sets the naming strategy to use for field names.
     *
     * @param namingStrategy the naming strategy to use
     */
    protected void setNamingStrategy(@NotNull NamingStrategy namingStrategy) {
        this.namingStrategy = namingStrategy;
    }
}
