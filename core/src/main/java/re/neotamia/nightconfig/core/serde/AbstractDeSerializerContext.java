package re.neotamia.nightconfig.core.serde;

import re.neotamia.nightconfig.core.serde.annotations.SerdeConfig;
import re.neotamia.nightconfig.core.serde.annotations.SerdeKey;

import java.lang.reflect.Field;

public abstract class AbstractDeSerializerContext {
    /**
     * Gets the naming strategy to use for field name transformation.
     * This method should be implemented by subclasses to provide access to their naming strategy.
     */
    protected abstract NamingStrategy getNamingStrategy();

    protected String configKey(Field field) {
        // Check for SerdeConfig first
        SerdeConfig configAnnot = field.getAnnotation(SerdeConfig.class);
        if (configAnnot != null) return AnnotationProcessor.resolveSerdeConfigKey(configAnnot, field.getName());

        // Check for standalone SerdeKey
        SerdeKey keyAnnot = field.getAnnotation(SerdeKey.class);
        if (keyAnnot != null) return keyAnnot.value();

        // No explicit key annotation found, apply naming strategy to the field name
        return getNamingStrategy().transformName(field.getName());
    }
}
