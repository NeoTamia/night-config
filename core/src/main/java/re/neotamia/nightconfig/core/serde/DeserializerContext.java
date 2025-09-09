package re.neotamia.nightconfig.core.serde;

import re.neotamia.nightconfig.core.NullObject;
import re.neotamia.nightconfig.core.UnmodifiableConfig;
import re.neotamia.nightconfig.core.serde.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Holds deserialization settings and provides some base deserialization logic.
 */
public final class DeserializerContext extends AbstractDeSerializerContext {
    final AbstractObjectDeserializer settings;

    DeserializerContext(AbstractObjectDeserializer settings) {
        this.settings = settings;
    }

    /**
     * Deserializes a single value in way that satisfies the given type constraint.
     *
     * @param value          value coming from the config that we are deserializing
     * @param typeConstraint the type that we want to produce
     * @return deserialization result
     * @throws SerdeException if no suitable deserializer is found
     */
    public Object deserializeValue(Object value, Optional<TypeConstraint> typeConstraint) {
        TypeConstraint t = typeConstraint.orElse(new TypeConstraint(Object.class));
        ValueDeserializer<Object, ?> deserializer = settings.findValueDeserializer(value, t);
        return deserializer.deserialize(value, typeConstraint, this);
    }

    /**
     * Deserializes a configuration by transforming its entries into fields of the
     * {@code destination} object.
     *
     * @param source      the config that we are deserializing
     * @param destination the object that we are modifying (result of the deserialization)
     */
    public void deserializeFields(UnmodifiableConfig source, Object destination) {
        // loop through the class hierarchy of the destination type
        Class<?> cls = destination.getClass();
        while (cls != Object.class) {
            for (Field field : cls.getDeclaredFields()) {
                if (preCheck(field)) {
                    // get the config key
                    List<String> path = Collections.singletonList(configKey(field));

                    // get the config value
                    Object value = source.getRaw(path);

                    // skip the field if the annotation say so
                    if (skipField(field, destination, value)) {
                        continue; // don't deserialize, go to the next field
                    }

                    // deserialize, but try the default value first
                    Object deserialized;
                    Supplier<?> defaultValueSupplier = settings.findDefaultValueSupplier(value, field, destination);
                    if (defaultValueSupplier != null) {
                        // default value found, use it directly
                        try {
                            deserialized = defaultValueSupplier.get();
                        } catch (Exception e) {
                            throw new SerdeException("Error in default value provider for field " + field, e);
                        }
                    } else {
                        // no default value, deserialize the config value
                        value = normalizeForDeserialization(value, path, field);

                        // find the right deserializer
                        TypeConstraint resultType = new TypeConstraint(field.getGenericType());
                        ValueDeserializer<Object, ?> deserializer = settings.findValueDeserializer(value, resultType);

                        // deserialize
                        try {
                            Optional<TypeConstraint> type = Optional.of(resultType);
                            deserialized = deserializer.deserialize(value, type, this);
                        } catch (Exception ex) {
                            throw new SerdeException("Error during deserialization of value `" + value + "` to field `" + field + "` with deserializer " + deserializer, ex);
                        }
                    }

                    // check the value of the field
                    if (!assertField(field, destination, deserialized)) {
                        throw new SerdeAssertException("Field `" + field + "` has an invalid value: " + deserialized);
                    }

                    // set the field
                    try {
                        field.set(destination, deserialized);
                    } catch (Exception e) {
                        throw new SerdeException("Could not assign the deserialized value `" + deserialized + "` to the field " + field + ". The original config value was " + value);
                    }
                }
            }
            cls = cls.getSuperclass();
        }
    }

    private Object normalizeForDeserialization(Object configValue, List<String> path, Field field) {
        if (configValue == null) {
            // missing value
            throw new SerdeException("Missing configuration entry " + path + " for field `" + field + "` declared in " + field.getDeclaringClass());
        } else if (configValue == NullObject.NULL_OBJECT) {
            // null value
            return null;
        }
        return configValue;
    }

    /**
     * @return true if the field should be skipped
     */
    @SuppressWarnings("unchecked")
    private boolean skipField(Field field, Object fieldContainer, Object rawConfigValue) {
        // Check for SerdeConfig first
        SerdeConfig configAnnot = field.getAnnotation(SerdeConfig.class);
        if (configAnnot != null) {
            try {
                // Check SerdeSkip within SerdeConfig
                Predicate<?> skipPredicate = AnnotationProcessor.resolveSerdeConfigSkipPredicate(configAnnot, fieldContainer, SerdePhase.DESERIALIZING, field);
                if (skipPredicate != null && ((Predicate<Object>) skipPredicate).test(rawConfigValue)) {
                    return true;
                }

                // Check SerdeSkipDeserializingIf within SerdeConfig
                skipPredicate = AnnotationProcessor.resolveSerdeConfigSkipDeserializingIfPredicate(configAnnot, fieldContainer);
                if (skipPredicate != null && ((Predicate<Object>) skipPredicate).test(rawConfigValue)) {
                    return true;
                }
            } catch (Exception e) {
                String msg = "Failed to resolve or apply skip predicate from SerdeConfig for deserialization of field " + field;
                throw new SerdeException(msg, e);
            }
        }

        // Check for standalone SerdeSkip
        SerdeSkip skipAnnot = field.getAnnotation(SerdeSkip.class);
        if (skipAnnot != null) {
            try {
                Predicate<?> skipPredicate = AnnotationProcessor.resolveSkipPredicate(skipAnnot, fieldContainer, SerdePhase.DESERIALIZING, field);
                if (((Predicate<Object>) skipPredicate).test(rawConfigValue)) {
                    return true;
                }
            } catch (Exception e) {
                String msg = "Failed to resolve or apply skip predicate for deserialization of field " + field;
                throw new SerdeException(msg, e);
            }
        }

        // Check for standalone SerdeSkipDeserializingIf
        SerdeSkipDeserializingIf annot = field.getAnnotation(SerdeSkipDeserializingIf.class);
        if (annot != null) {
            try {
                Predicate<?> skipPredicate = AnnotationProcessor.resolveSkipDeserializingIfPredicate(annot, fieldContainer);
                return ((Predicate<Object>) skipPredicate).test(rawConfigValue);
            } catch (Exception e) {
                String msg = "Failed to resolve or apply skip predicate for deserialization of field " + field;
                throw new SerdeException(msg, e);
            }
        }

        return false;
    }

    /**
     * @return false if there is an assertion and it fails
     */
    @SuppressWarnings("unchecked")
    private boolean assertField(Field field, Object fieldContainer, Object fieldValue) {
        // Check for SerdeConfig first
        SerdeConfig configAnnot = field.getAnnotation(SerdeConfig.class);
        if (configAnnot != null) {
            try {
                Predicate<?> assertPredicate = AnnotationProcessor.resolveSerdeConfigAssertPredicate(configAnnot, fieldContainer, SerdePhase.DESERIALIZING, field);
                if (assertPredicate != null && !((Predicate<Object>) assertPredicate).test(fieldValue))
                    return false;
            } catch (Exception e) {
                String msg = "Failed to resolve or apply assertion from SerdeConfig for deserialization of field " + field;
                throw new SerdeException(msg, e);
            }
        }

        // Check for standalone SerdeAssert annotations
        SerdeAssert[] annot = field.getAnnotationsByType(SerdeAssert.class);
        if (annot.length == 0) {
            return true;
        }
        try {
            Predicate<?> assertPredicate = AnnotationProcessor.resolveAssertPredicate(annot, fieldContainer,
                    SerdePhase.DESERIALIZING, field);
            if (assertPredicate == null) {
                return true;
            }
            return ((Predicate<Object>) assertPredicate).test(fieldValue);
        } catch (Exception e) {
            String msg = "Failed to resolve or apply assertion for deserialization of field " + field;
            throw new SerdeException(msg, e);
        }
    }

    private boolean preCheck(Field field) {
        int mods = field.getModifiers();
        if (Modifier.isStatic(mods) || field.isSynthetic()) {
            return false;
        }
        if (Modifier.isTransient(mods) && settings.applyTransientModifier) {
            return false;
        }
        if (Modifier.isFinal(mods) || !Modifier.isPublic(mods)) {
            field.setAccessible(true);
        }
        return true;
    }

}
