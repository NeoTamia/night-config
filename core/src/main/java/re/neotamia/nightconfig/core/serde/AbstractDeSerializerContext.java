package re.neotamia.nightconfig.core.serde;

import re.neotamia.nightconfig.core.serde.annotations.SerdeConfig;
import re.neotamia.nightconfig.core.serde.annotations.SerdeKey;

import java.lang.reflect.Field;

public abstract class AbstractDeSerializerContext {
     protected String configKey(Field field) {
        // Check for SerdeConfig first
        SerdeConfig configAnnot = field.getAnnotation(SerdeConfig.class);
        if (configAnnot != null) return AnnotationProcessor.resolveSerdeConfigKey(configAnnot, field.getName());

        // Check for standalone SerdeKey
        SerdeKey keyAnnot = field.getAnnotation(SerdeKey.class);
        return keyAnnot == null ? field.getName() : keyAnnot.value();
    }
}
