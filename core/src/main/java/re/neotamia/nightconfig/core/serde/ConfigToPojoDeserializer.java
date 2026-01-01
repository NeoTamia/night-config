package re.neotamia.nightconfig.core.serde;

import org.jetbrains.annotations.Nullable;
import re.neotamia.nightconfig.core.NullObject;
import re.neotamia.nightconfig.core.UnmodifiableConfig;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Deserialize a {@code Config} to the fields of a Plain-Old-Java-Object (POJO).
 */
final class ConfigToPojoDeserializer implements ValueDeserializer<UnmodifiableConfig, Object> {

	@Override
	public Object deserialize(UnmodifiableConfig value, @Nullable TypeConstraint resultType, DeserializerContext ctx) {
		if (resultType == null) {
			// no constraint, we don't know the type of the POJO!
			// Assume the easiest result: return the value as is
			return value;
		} else {
            Class<?> cls = resultType.getSatisfyingRawType().orElseThrow(() -> new SerdeException("Could not find a concrete type that can satisfy the constraint " + resultType));

			if (cls.isRecord()) return deserializeToRecord(value, cls, resultType, ctx);
			return deserializeToNormalClass(value, cls, ctx);
		}
	}

	private Object deserializeToNormalClass(UnmodifiableConfig value, Class<?> cls, DeserializerContext ctx) {
		Object instance;
		try {
			Constructor<?> constructor = cls.getDeclaredConstructor();
			if (!Modifier.isPublic(constructor.getModifiers())) {
				constructor.setAccessible(true);
			}
			instance = constructor.newInstance();
		} catch (Exception e) {
			throw new SerdeException("Failed to create an instance of " + cls, e);
		}
		ctx.deserializeFields(value, instance);
		return instance;
	}

	private Object deserializeToRecord(UnmodifiableConfig value, Class<?> objectClass, TypeConstraint resultTypeConstraint, DeserializerContext ctx) {
		var components = objectClass.getRecordComponents();
		var constructor = getCanonicalRecordConstructor(objectClass, components);
		var componentValues = new Object[components.length];

		Map<TypeVariable<?>, Type> typeMap = Collections.emptyMap();
		TypeConstraint[] typeArgs = resultTypeConstraint.resolveTypeArgumentsFor(objectClass).orElse(null);
		TypeVariable<?>[] typeVars = objectClass.getTypeParameters();
		if (typeArgs != null && typeVars.length > 0) {
			typeMap = new HashMap<>();
			for (int i = 0; i < typeVars.length; i++) {
				typeMap.put(typeVars[i], typeArgs[i].getFullType());
			}
		}

		for (int i = 0; i < components.length; i++) {
			RecordComponent comp = components[i];
			Type compType = comp.getGenericType();
			if (!typeMap.isEmpty()) {
				compType = ctx.resolveType(compType, typeMap);
			}
			TypeConstraint componentConstraint = new TypeConstraint(compType);

			Object configValue = value.getRaw(Collections.singletonList(comp.getName()));
			if (configValue == null) {
				// missing component!
				// find all the missing components to emit a more helpful error message
				List<String> missingComponents = Arrays.stream(components).map(RecordComponent::getName)
					.filter(c -> !value.contains(c)).collect(Collectors.toList());
				var missingComponentsStr = String.join(", ", missingComponents);
				throw new SerdeException("Could not deserialize this configuration to a record of type " + objectClass
						+ " because the following components (entries) are missing: " + missingComponentsStr);
			}
			if (configValue == NullObject.NULL_OBJECT) {
				// component of value null
				configValue = null;
			}
			componentValues[i] = (configValue == null) ? null : ctx.deserializeValue(configValue, componentConstraint);
		}
		try {
			return constructor.newInstance(componentValues);
		} catch (Exception e) {
			throw new SerdeException("Failed to create an instance of record " + objectClass, e);
		}
	}

	private static Constructor<?> getCanonicalRecordConstructor(Class<?> cls, RecordComponent[] components) {
		Class<?>[] paramTypes = Arrays.stream(components)
			.map(RecordComponent::getType)
			.toArray(Class<?>[]::new);
		try {
			return cls.getDeclaredConstructor(paramTypes);
		} catch (Exception e) {
			throw new SerdeException("Failed to get the canonical constructor of record " + cls, e);
		}
	}
}
