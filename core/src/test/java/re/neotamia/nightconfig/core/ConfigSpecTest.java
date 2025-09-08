package re.neotamia.nightconfig.core;

import java.util.Arrays;
import java.util.Map;

import re.neotamia.sharedtests.TestEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author TheElectronWill
 */
class ConfigSpecTest {

	@Test
	public void test() {
		ConfigSpec spec = new ConfigSpec();
		spec.define("a.b.string", "");
		spec.define("a.b.int", 20);
		spec.defineInRange("a.i", 0, -20, 20);
		spec.defineInRange("a.l", 0, -20, 20);
		spec.defineInRange("a.f", 0.1f, -0.2f, 0.2f);
		spec.defineInRange("a.d", 0.1, -0.1, 0.2);
		spec.defineInList("a.s", "default", Arrays.asList("a", "b", "c", "d", "e", "f", "default"));
		spec.defineList("a.list", Arrays.asList("1", "2"), element -> element instanceof String);
		spec.defineRestrictedEnum("a.enum1", TestEnum.A, Arrays.asList(TestEnum.A, TestEnum.B),
			EnumGetMethod.ORDINAL_OR_NAME_IGNORECASE);
		spec.defineRestrictedEnum("a.enum2", TestEnum.A, Arrays.asList(TestEnum.A, TestEnum.B),
			EnumGetMethod.ORDINAL_OR_NAME_IGNORECASE);
		spec.defineRestrictedEnum("a.enum3", TestEnum.A, Arrays.asList(TestEnum.A, TestEnum.B),
			EnumGetMethod.ORDINAL_OR_NAME_IGNORECASE);

		{
			Config config = Config.inMemory();
			config.set("a.i", 256);
			config.set("a.l", 1234567890);
			config.set("a.f", 12f);
			config.set("a.d", 123d);
			config.set("a.s", "value");
			config.set("a.list", Arrays.asList("hey", null, false, 1));
			config.set("a.enum1", null);
			config.set("a.enum2", -1);
			config.set("a.enum3", 3);
			config.set("a.enum4", "C");

			assertFalse(spec.isCorrect(config));
			System.out.println("Before correction: " + configToString(config));
			spec.correct(config);
			System.out.println("After correction: " + configToString(config));
			assertTrue(spec.isCorrect(config), "spec.correct hasn't corrected the config properly");
		}

		{
			Config config = Config.inMemory();
			config.set("a.b.string", "some string");
			config.set("a.b.int", 123456789);
			config.set("a.i", 18);
			config.set("a.l", 18);
			config.set("a.f", 0.15f);
			config.set("a.d", -0.09);
			config.set("a.s", "a");
			config.set("a.list", Arrays.asList("test", "", "."));
			config.set("a.enum1", TestEnum.B);
			config.set("a.enum2", "b");
			config.set("a.enum2", "B");
			config.set("a.enum3", 1);

			assertTrue(spec.isCorrect(config));
			System.out.println("Before correction: " + configToString(config));
			spec.correct(config);
			System.out.println("After correction: " + configToString(config));
			assertTrue(spec.isCorrect(config), "spec.correct introduced errors in the config");
		}
	}

	private static String configToString(Config c) {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		for (Map.Entry<String, Object> entry : c.valueMap().entrySet()) {
			final String key = entry.getKey();
			final Object value = entry.getValue();
			sb.append(key).append(" = ");
			if (value instanceof Config) {
				sb.append(configToString((Config)value));
			} else {
				sb.append(value);
			}
			sb.append(", ");
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append('}');
		return sb.toString();
	}

	@Test
	public void testPrimitiveConversions() {
		ConfigSpec spec = new ConfigSpec();
		spec.defineInRange("x", 1L, 1L, 100L);

		Config conf = Config.inMemory();
		conf.set("x", 2L);
		assertTrue(spec.isCorrect(conf));
		assertEquals(0, spec.correct(conf));

		spec = new ConfigSpec();
		spec.defineInRange("x", 1, 1, 100);
		conf.set("x", 2L);
		assertTrue(spec.isCorrect(conf));
		assertEquals(0, spec.correct(conf));

		spec = new ConfigSpec();
		spec.defineInRange("x", 1.0, 1.0, 100.0);
		conf.set("x", 2L);
		assertTrue(spec.isCorrect(conf));
		assertEquals(0, spec.correct(conf));

		spec = new ConfigSpec();
		spec.defineInRange("x", 1.0, 1.0, 100.0);
		conf.set("x", 2);
		assertTrue(spec.isCorrect(conf));
		assertEquals(0, spec.correct(conf));

		spec = new ConfigSpec();
		spec.defineInRange("x", 1.0f, 1.0f, 100.0f);
		conf.set("x", 2L);
		assertTrue(spec.isCorrect(conf));
		assertEquals(0, spec.correct(conf));

		spec = new ConfigSpec();
		spec.defineInRange("x", 1.0f, 1.0f, 100.0f);
		conf.set("x", 2);
		assertTrue(spec.isCorrect(conf));
		assertEquals(0, spec.correct(conf));

		spec = new ConfigSpec();
		spec.defineInRange("x", 1, 1, 100);
		conf.set("x", 2.0);
		assertTrue(spec.isCorrect(conf));
		assertEquals(0, spec.correct(conf));

		spec = new ConfigSpec();
		spec.defineInRange("x", 1, 1, 100);
		conf.set("x", 2.0f);
		assertTrue(spec.isCorrect(conf));
		assertEquals(0, spec.correct(conf));
	}

}
