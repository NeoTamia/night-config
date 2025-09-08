package re.neotamia.nightconfig.core.conversion;

/**
 * Performs conversions between field values and config values.
 *
 * @deprecated Use the new package {@link re.neotamia.nightconfig.core.serde}.
 * @author TheElectronWill
 */
@Deprecated
public interface Converter<FieldType, ConfigValueType> {
	FieldType convertToField(ConfigValueType value);

	ConfigValueType convertFromField(FieldType value);
}
