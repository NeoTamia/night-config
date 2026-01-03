package re.neotamia.nightconfig.core.serde;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import re.neotamia.nightconfig.core.Config;
import re.neotamia.nightconfig.core.EnumGetMethod;
import re.neotamia.nightconfig.core.UnmodifiableConfig;

final class StandardDeserializers {
	private StandardDeserializers() {}

	/**
	 * The trivial deserializer: deserialize(value) == value.
	 */
    static final class TrivialDeserializer implements ValueDeserializer<Object, Object> {
		@Override
		public @NotNull Object deserialize(Object value, TypeConstraint resultType, @NotNull DeserializerContext ctx) {
			return value;
		}
	}

	/**
	 * Deserializes {@code Map<String, Value>} or {@code UnmodifiableConfig} to {@code Map<String, Result>}.
	 */
	static final class MapDeserializer implements ValueDeserializer<Object, Map<String, ?>> {

		@Override
		public @NotNull Map<String, ?> deserialize(Object mapValue, TypeConstraint resultType, @NotNull DeserializerContext ctx) {

			int size;
			if (mapValue instanceof UnmodifiableConfig) {
				size = ((UnmodifiableConfig) mapValue).size();
			} else {
				size = ((Map<?, ?>) mapValue).size();
			}

			// Look for the type of the values to insert in the map,
			// and create a map of the right type.

			Optional<TypeConstraint[]> mapKVType;
			Map<String, Object> res;
			if (resultType != null) {
                res = createMapInstance(resultType.getSatisfyingRawType().get(), size);
				mapKVType = extractMapKVType(resultType);
			} else {
				mapKVType = Optional.empty();
				res = Config.isInsertionOrderPreserved() ? new java.util.LinkedHashMap<>(size) : new java.util.HashMap<>(size);
			}

			// separate types of Key and Value
			Optional<TypeConstraint> mapKeyType = mapKVType.map(arr -> arr[0]);
			Optional<TypeConstraint> mapValueType = mapKVType.map(arr -> arr[1]);

			// check the type of keys
			if (mapKeyType.isPresent()) {
				if (!mapKeyType.get().getSatisfyingRawType().equals(Optional.of(String.class))) {
					throw new SerdeException(
							"Invalid map type for deserialization, the keys should be of type String instead of "
									+ mapKeyType.get() + ". Full map type: " + resultType);
				}
			}

			if (mapValue instanceof UnmodifiableConfig) {
				// deserialize config entries to map values, converting each value
				for (UnmodifiableConfig.Entry entry : ((UnmodifiableConfig) mapValue).entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();
					Object deserialized = ctx.deserializeValue(value, mapValueType.orElse(null));
					res.put(key, deserialized);
				}
			} else {
				// deserialize map entries to map values, converting each value
				for (Map.Entry<?, ?> entry : ((Map<?, ?>) mapValue).entrySet()) {
					Object key = entry.getKey();
					if (!(key instanceof String)) {
						String keyClassStr = key == null ? "null" : key.getClass().toString();
						throw new SerdeException(
								"Invalid map type for deserialization, the keys should be of type String instead of "
										+ keyClassStr + ". Full map type: " + resultType);
					}
					Object value = entry.getValue();
					Object deserialized = ctx.deserializeValue(value, mapValueType.orElse(null));
					res.put((String) key, deserialized);
				}
			}
			return res;
		}

		private static Optional<TypeConstraint[]> extractMapKVType(TypeConstraint mapTypeC) {
			// return collType.resolveTypeArgumentsFor(Collection.class).map(c -> c[0]).orElse(null);
			return mapTypeC.resolveTypeArgumentsFor(Map.class);
		}

		@SuppressWarnings("unchecked")
		private static Map<String, Object> createMapInstance(Class<?> cls, int sizeHint) {
			if (cls == Map.class) {
				return Config.isInsertionOrderPreserved() ? new java.util.LinkedHashMap<>(sizeHint) : new java.util.HashMap<>(sizeHint);
			}
			if (cls == java.util.LinkedHashMap.class) {
				return new java.util.LinkedHashMap<>(sizeHint);
			}
			if (cls == java.util.HashMap.class) {
				return new java.util.HashMap<>(sizeHint);
			}
			if (cls == java.util.IdentityHashMap.class) {
				return new java.util.IdentityHashMap<>(sizeHint);
			}
			if (cls.isAssignableFrom(java.util.HashMap.class)) {
				// We use isAssignableFrom to cover other superclasses or superinterfaces of HashMap,$
				// such as NavigableMap.
				if (Config.isInsertionOrderPreserved() && cls.isAssignableFrom(java.util.LinkedHashMap.class)) {
					return new java.util.LinkedHashMap<>(sizeHint);
				} else {
					return new java.util.HashMap<>(sizeHint);
				}
			}
			if (cls.isAssignableFrom(java.util.concurrent.ConcurrentHashMap.class)) {
				return new java.util.concurrent.ConcurrentHashMap<>(sizeHint);
			}

			// unknown Map type, try the public parameterless constructor
			try {
				return (Map<String, Object>) cls.getDeclaredConstructor().newInstance();
			} catch (Exception ex) {
				throw new SerdeException("Failed to create an instance of " + cls, ex);
			}
		}
	}

	/**
	 * Deserializes {@code Collection<Value>} to {@code Collection<Result>}.
	 */
	static final class CollectionDeserializer implements ValueDeserializer<Collection<?>, Collection<?>> {

		@Override
		public @NotNull Collection<?> deserialize(Collection<?> collectionValue, TypeConstraint resultType, @NotNull DeserializerContext ctx) {
			int size = collectionValue.size();
			Collection<Object> res;
			Optional<TypeConstraint> valueType;
			if (resultType != null) {
                res = createCollectionInstance(resultType.getSatisfyingRawType().get(), size);
				valueType = extractCollectionValueType(resultType);
			} else {
				// no constraint, choose arbitrarily: it will be ArrayList
				res = new ArrayList<>(size);
				valueType = Optional.empty();
			}

			// convert the values
			for (Object v : collectionValue) {
				Object deserialized = ctx.deserializeValue(v, valueType.orElse(null));
				res.add(deserialized);
			}
			return res;
		}

		@SuppressWarnings("unchecked")
		private Collection<Object> createCollectionInstance(Class<?> cls, int sizeHint) {
			if (cls.isAssignableFrom(java.util.ArrayList.class)) {
				return new java.util.ArrayList<>(sizeHint);
			}
			if (cls.isAssignableFrom(java.util.LinkedList.class)) {
				return new java.util.LinkedList<>();
			}
			if (cls.isAssignableFrom(java.util.ArrayDeque.class)) {
				return new java.util.ArrayDeque<>(sizeHint);
			}

			// unknown Collection type, try the public parameterless constructor
			try {
				return (Collection<Object>) cls.getDeclaredConstructor().newInstance();
			} catch (Exception ex) {
				throw new SerdeException("Failed to create an instance of " + cls, ex);
			}
		}

		private static Optional<TypeConstraint> extractCollectionValueType(TypeConstraint collType) {
			return collType.resolveTypeArgumentsFor(Collection.class).map(c -> c[0]);
		}
	}

	/**
	 * Deserializes a {@code Collection<V>} to an {@code Array<R>} (i.e. {@code R[]}).
	 */
	static final class CollectionToArrayDeserializer implements ValueDeserializer<Collection<?>, Object> {

		@Override
		public @NotNull Object deserialize(Collection<?> collectionValue, TypeConstraint resultType, @NotNull DeserializerContext ctx) {

			int size = collectionValue.size();
			Object res;
			Optional<TypeConstraint> valueType;
			if (resultType != null) {
                Class<?> componentType = ((Class<?>) resultType.getFullType()).getComponentType();
				assert componentType != null;
				res = Array.newInstance(componentType, size);
				valueType = Optional.of(new TypeConstraint(componentType));
			} else {
				// no constraint, choose arbitrarily: it will be Object[]
				res = new Object[size];
				valueType = Optional.empty();
			}

			// convert the values
			int i = 0;
			for (Object v : collectionValue) {
				Object deserialized = ctx.deserializeValue(v, valueType.orElse(null));
				Array.set(res, i, deserialized);
				i++;
			}
			return res;
		}
	}

	/**
	 * Deserializes a {@code String} into an {@code Enum}.
	 */
	static final class EnumDeserializer implements ValueDeserializer<String, Enum<?>> {

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public @NotNull Enum<?> deserialize(String value, TypeConstraint resultType, @NotNull DeserializerContext ctx) {
            if (resultType == null) {
                throw new SerdeException("Cannot deserialize a value to an enum without knowing the enum type");
            }
            Class<?> cls = resultType.getSatisfyingRawType()
					.orElseThrow(() -> new SerdeException("Could not find a concrete enum type that can satisfy the constraint " + resultType));
			// TODO use the field's annotations, if any, to get the right variant of EnumGetMethod
			return EnumGetMethod.NAME.get(value, (Class) cls);
		}
	}

	/**
	 * Deserializes a {@code String} into a {@code UUID}.
	 */
	static final class UuidDeserializer implements ValueDeserializer<String, UUID> {
		@Override
		public @NotNull UUID deserialize(String value, TypeConstraint resultType, @NotNull DeserializerContext ctx) {
			return UUID.fromString(value);
		}
	}

	/**
	 * Tries to perform a "risky" conversion between two number types.
	 * Throws an exception is the conversion turns out to be lossy.
	 * <p>
	 * This deserializer should only be called if the value cannot be assigned to the number field
	 * using an automatic widening conversion.
	 */
	static final class RiskyNumberDeserializer implements ValueDeserializer<Number, Number> {

		public static boolean isNumberTypeSupported(Class<?> t) {
			return t == Integer.class || t == int.class || t == Long.class || t == long.class
                    || t == Double.class || t == double.class || t == Float.class || t == float.class;
		}

		@Override
		public @NotNull Number deserialize(Number value, TypeConstraint resultType, @NotNull DeserializerContext ctx) {
            if (resultType == null)
                throw new SerdeException("Cannot deserialize a value with a risky number conversion without knowing the number type");
			Class<?> resultCls = resultType.getSatisfyingRawType()
					.orElseThrow(() -> new SerdeException("Could not find a concrete number type that can satisfy the constraint " + resultType));

            if (resultCls == int.class || resultCls == Integer.class) {
                int i = value.intValue();
                if (value instanceof Float || value instanceof Double) {
                    if (value.doubleValue() == (double)i) return i;
                } else if (value.longValue() == (long)i) {
                    return i;
                }
            } else if (resultCls == long.class || resultCls == Long.class) {
                long l = value.longValue();
                if (value instanceof Float || value instanceof Double) {
                    if (value.doubleValue() == (double)l) return l;
                } else {
                    return l;
                }
            } else if (resultCls == short.class || resultCls == Short.class) {
                short s = value.shortValue();
                if (value instanceof Float || value instanceof Double) {
                    if (value.doubleValue() == (double)s) return s;
                } else if (value.longValue() == (long)s) {
                    return s;
                }
            } else if (resultCls == byte.class || resultCls == Byte.class) {
                byte b = value.byteValue();
                if (value instanceof Float || value instanceof Double) {
                    if (value.doubleValue() == (double)b) return b;
                } else if (value.longValue() == (long)b) {
                    return b;
                }
            } else if (resultCls == float.class || resultCls == Float.class) {
                float f = value.floatValue();
                if (value.doubleValue() == (double)f) return f;
            } else if (resultCls == double.class || resultCls == Double.class) {
                return value.doubleValue();
            }
			throw new SerdeException(String.format("Cannot deserialize %s (%s) to %s: the conversion would be lossy", value, value.getClass().getSimpleName(), resultCls.getSimpleName()));
		}
	}

}
