package re.neotamia.nightconfig.core.serde.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A configuration annotation that consolidates multiple Serde annotations
 * into a single annotation for simplified usage.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SerdeConfig {
    /**
     * Assertions to apply to the field during (de)serialization. <br/>
     *
     * @return the assertion to apply to the annotated field
     */
    SerdeAssert[] asserts() default {};

    /**
     * Comments to add to the field in the serialized config.
     *
     * @return the comments to add
     */
    SerdeComment[] comments() default {};

    /**
     * Default values to apply to the field during (de)serialization if the field is
     * missing in the config. <br/>
     *
     * @return the default values to apply
     */
    SerdeDefault[] defaults() default {};

    /**
     * Key to use in the config for the annotated field. <br/>
     * Only one {@link SerdeKey}
     *
     * @return the key to use
     */
    String key() default "";

    /**
     * Skip rules to apply during (de)serialization. <br />
     * Only one {@link SerdeSkip}
     *
     * @return the skip rules to apply
     */
    SerdeSkip[] skip() default {};

    /**
     * Skip rules to apply during deserialization only. <br/>
     * Only one {@link SerdeSkipDeserializingIf}
     *
     * @return the skip rules to apply during deserialization
     */
    SerdeSkipDeserializingIf[] skipDeserializingIf() default {};

    /**
     * Skip rules to apply during serialization only. <br/>
     * Only one {@link SerdeSkipSerializingIf}
     *
     * @return the skip rules to apply during serialization
     */
    SerdeSkipSerializingIf[] skipSerializingIf() default {};
}
