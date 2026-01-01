package re.neotamia.nightconfig.core.serde;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import re.neotamia.nightconfig.core.ConfigFormat;
import re.neotamia.nightconfig.core.UnmodifiableConfig;

/**
 * Builder for {@link ObjectSerializer}.
 */
public final class ObjectSerializerBuilder {
    final Map<Type, ValueSerializer<?, ?>> classBasedSerializers = new HashMap<>(7);

    final List<ValueSerializerProvider<?, ?>> generalProviders = new ArrayList<>();

    /** the last-resort serializer provider, used when no other provider matches */
    ValueSerializerProvider<?, ?> defaultProvider = NoProvider.INSTANCE;

    /** setting: skip transient fields as requested by the modifier */
    boolean applyTransientModifier = true;

    /**
     * strategy for transforming field names, defaults to {@link NamingStrategy#IDENTITY}
     */
    @NotNull
    NamingStrategy namingStrategy = NamingStrategy.IDENTITY;

    ObjectSerializerBuilder(boolean standards) {
        if (standards)
            registerStandardSerializers();
    }

    public ObjectSerializer build() {
        return new ObjectSerializer(this);
    }

    public <V, R> ObjectSerializerBuilder withSerializerForExactClass(Class<V> cls, ValueSerializer<? super V, ? extends R> serializer) {
        classBasedSerializers.put(cls, serializer);
        return this;
    }

    public <V, R> ObjectSerializerBuilder withSerializerForType(Type type, ValueSerializer<? super V, ? extends R> serializer) {
        return withSerializerProvider((valueType, ctx) -> {
            if (new TypeConstraint(valueType).isAssignableTo(type)) {
                return (ValueSerializer) serializer;
            }
            return null;
        });
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <V, R> ObjectSerializerBuilder withSerializerForClass(Class<V> cls, ValueSerializer<? super V, ? extends R> serializer) {
        generalProviders.add(
                (valueType, ctx) -> {
                    Class<?> valueClass = new TypeConstraint(valueType).getSatisfyingRawType().orElse(null);
                    return valueClass != null && Util.canAssign(cls, valueClass)
                        ? (ValueSerializer) serializer
                        : null;
                });
        return this;
    }

    public <V, R> ObjectSerializerBuilder withSerializerProvider(ValueSerializerProvider<V, R> provider) {
        generalProviders.add(0, provider);
        return this;
    }

    public <V, R> ObjectSerializerBuilder withDefaultSerializerProvider(ValueSerializerProvider<V, R> provider) {
        defaultProvider = provider;
        return this;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ObjectSerializerBuilder withDefaultSerializerProvider() {
        ValueSerializer trivialSer = new StandardSerializers.TrivialSerializer();
        ValueSerializer fieldsSer = new StandardSerializers.FieldsToConfigSerializer();
        ValueSerializer numberToIntSer = (value, ctx) -> ((Number) value).intValue();
        ValueSerializer charToIntSer = (value, ctx) -> (int) (Character) value;

        defaultProvider = (valueType, ctx) -> {
            ConfigFormat<?> format = ctx.configFormat();
            Class<?> valueClass = new TypeConstraint(valueType).getSatisfyingRawType().orElse(null);
            Class<?> wrapperClass = Util.toWrapper(valueClass);
            if (format == null || (wrapperClass != null && format.supportsType(wrapperClass))) {
                return trivialSer;
            } else if (valueClass != null && (Util.isPrimitiveOrWrapper(valueClass) || valueClass == String.class || valueClass.isArray())) {
                // Cannot access the fields of the value!
                // try to convert to int, if supported
                if (format.supportsType(int.class)) {
                    if (Util.canAssign(int.class, valueClass)) {
                        if (valueClass == Character.class || valueClass == char.class) {
                            return charToIntSer;
                        } else {
                            return numberToIntSer;
                        }
                    }
                }
                // no possible conversion, fail
                return null;
            } else {
                // type not supported, convert to subconfig with fields
                return fieldsSer;
            }
        };
        return this;
    }

	/**
	 * Serialize transient fields instead of ignoring them.
	 */
    public ObjectSerializerBuilder serializeTransientFields() {
        this.applyTransientModifier = false;
        return this;
    }

    /**
     * Sets the naming strategy to use for transforming field names.
     *
     * @param strategy the naming strategy to use
     * @return this builder for method chaining
     */
    public ObjectSerializerBuilder withNamingStrategy(@NotNull NamingStrategy strategy) {
        this.namingStrategy = strategy;
        return this;
    }

    /** registers the standard serializers */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void registerStandardSerializers() {
        withDefaultSerializerProvider();

        ValueSerializer mapSer = new StandardSerializers.MapSerializer();
        ValueSerializer collSer = new StandardSerializers.CollectionSerializer();
        ValueSerializer iterSer = new StandardSerializers.IterableSerializer();
        ValueSerializer arraySer = new StandardSerializers.ArraySerializer();
        ValueSerializer enumSer = new StandardSerializers.EnumSerializer();
        ValueSerializer trivialSer = new StandardSerializers.TrivialSerializer();
		ValueSerializer uuidSer = new StandardSerializers.UuidSerializer();

        withSerializerProvider((valueType, ctx) -> {
            Class<?> valueClass = new TypeConstraint(valueType).getSatisfyingRawType().orElse(null);
            if (valueClass == null) {
                ConfigFormat<?> format = ctx.configFormat();
                if (format == null || format.supportsType(null)) {
                    return trivialSer;
                } else {
                    return null;
                }
            }
            if (Map.class.isAssignableFrom(valueClass)) {
                Type valType = null;
                if (valueType instanceof ParameterizedType pt) {
                    Type[] args = pt.getActualTypeArguments();
                    if (args.length == 2) valType = args[1];
                }
                return (ValueSerializer) (valType == null ? mapSer : new StandardSerializers.MapSerializer(valType));
            }
            if (Collection.class.isAssignableFrom(valueClass)) {
                Type elType = null;
                if (valueType instanceof ParameterizedType pt) {
                    Type[] args = pt.getActualTypeArguments();
                    if (args.length == 1) elType = args[0];
                }
                return (ValueSerializer) (elType == null ? collSer : new StandardSerializers.CollectionSerializer(elType));
            }
            if (Iterable.class.isAssignableFrom(valueClass)) {
                Type elType = null;
                if (valueType instanceof ParameterizedType pt) {
                    Type[] args = pt.getActualTypeArguments();
                    if (args.length == 1) elType = args[0];
                }
                return (ValueSerializer) (elType == null ? iterSer : new StandardSerializers.IterableSerializer(elType));
            }
            if (UnmodifiableConfig.class.isAssignableFrom(valueClass)) {
                return trivialSer; // the value is already a config, nothing to serialize
            }
            if (Enum.class.isAssignableFrom(valueClass)) {
                return enumSer;
            }
            if (valueType instanceof GenericArrayType gat) {
                return new StandardSerializers.ArraySerializer(gat.getGenericComponentType());
            }
            if (valueClass.isArray()) {
                return new StandardSerializers.ArraySerializer(valueClass.getComponentType());
            }
            if (valueClass == UUID.class) {
                return uuidSer;
            }
            return null;
        });
    }

    /** A provider that provides nothing, {@code provide} always returns null. */
    static final class NoProvider implements ValueSerializerProvider<Object, Object> {
        static final NoProvider INSTANCE = new NoProvider();

        @Override
        public ValueSerializer<Object, Object> provide(Type valueType, SerializerContext ctx) {
            return null;
        }
    }
}
