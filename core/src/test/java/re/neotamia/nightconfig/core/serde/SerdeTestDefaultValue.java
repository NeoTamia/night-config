package re.neotamia.nightconfig.core.serde;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import re.neotamia.nightconfig.core.Config;
import re.neotamia.nightconfig.core.serde.annotations.*;
import re.neotamia.nightconfig.core.serde.annotations.SerdeDefault;
import re.neotamia.nightconfig.core.serde.annotations.SerdeDefault.WhenValue;

public final class SerdeTestDefaultValue {
	static class DefaultValueAnnotations1 {
		@SerdeDefault(provider = "defaultServers", whenValue = WhenValue.IS_MISSING)
		List<String> servers = null;

		List<String> defaultServers() {
			return List.of("example.org");
		}
	}

	static class DefaultValueAnnotations2 {
		@SerdeDefault(provider = "defaultServers", whenValue = WhenValue.IS_MISSING)
		List<String> servers = null;

		static List<String> defaultServers() {
			return List.of("example.org");
		}
	}

	static class DefaultValueAnnotations3 {
		@SerdeDefault(provider = "defaultServers", whenValue = WhenValue.IS_MISSING)
		List<String> servers = null;

		@SuppressWarnings("unused")
		private final transient Supplier<List<String>> defaultServers = () -> List.of("example.org");
	}

	private void testDefaultServers(Supplier<?> sup) throws Exception {
		testDefaultServers(sup, List.of("example.org"));
	}

	private void testDefaultServers(Supplier<?> sup, List<String> defaultValue) throws Exception {
		Field serversField = sup.get().getClass().getDeclaredField("servers");
		var ser = ObjectSerializer.builder().build();
		var de = ObjectDeserializer.builder().build();

		// IS_MISSING only applies to DEserialization, nothing changes when serializing
		var o = sup.get();
        var serialized = ser.serializeFields(o, Config::inMemory);
		assertNull(serialized.get("servers"));

		o = sup.get();
		serversField.set(o, List.of("wow"));
        serialized = ser.serializeFields(o, Config::inMemory);
		assertEquals(List.of("wow"), serialized.get("servers"));

		// check deserialization now
		var conf = Config.inMemory(); // empty
		var deserialized = de.deserializeFields(conf, sup);
		assertEquals(defaultValue, serversField.get(deserialized));

		conf.set("servers", null);
		deserialized = de.deserializeFields(conf, sup);
		assertNull(serversField.get(deserialized));

		conf.set("servers", List.of("amazing-server"));
		deserialized = de.deserializeFields(conf, sup);
		assertEquals(List.of("amazing-server"), serversField.get(deserialized));
	}

	@Test
	public void providerInClass() throws Exception {
		testDefaultServers(DefaultValueAnnotations1::new);
		testDefaultServers(DefaultValueAnnotations2::new);
		testDefaultServers(DefaultValueAnnotations3::new);

	}

	static class DefaultProvidersA {
		static final Supplier<List<String>> defaultServersSupplier = () -> List.of("example.org");

		static List<String> defaultServersFunction() {
			return List.of("example.org");
		}
	}

	static class DefaultValueAnnotations4 {
		@SerdeDefault(provider = "defaultServersSupplier", cls = DefaultProvidersA.class, whenValue = WhenValue.IS_MISSING)
		List<String> servers = null;
	}

	static class DefaultValueAnnotations5 {
		@SerdeDefault(provider = "defaultServersFunction", cls = DefaultProvidersA.class, whenValue = WhenValue.IS_MISSING)
		List<String> servers = null;
	}

	@Test
	public void providerInAnotherClass() throws Exception {
		testDefaultServers(DefaultValueAnnotations4::new);
		testDefaultServers(DefaultValueAnnotations5::new);
	}

	static class DefaultProvidersB {
		static final Supplier<List<String>> defaultServers = () -> List.of("default-from-field");

		static List<String> defaultServers() {
			return List.of("default-from-method");
		}
	}

	static class DefaultValueAnnotations6 {
		@SerdeDefault(provider = "defaultServers", cls = DefaultProvidersB.class, whenValue = WhenValue.IS_MISSING)
		List<String> servers = null;
	}

	static class DefaultValueAnnotations7 {
		@SerdeDefault(provider = "defaultServers()", cls = DefaultProvidersB.class, whenValue = WhenValue.IS_MISSING)
		List<String> servers = null;
	}

	@Test
	public void providerInAnotherClassWithExplicitDisambiguation() throws Exception {
		testDefaultServers(DefaultValueAnnotations6::new, List.of("default-from-field"));
		testDefaultServers(DefaultValueAnnotations7::new, List.of("default-from-method"));
	}

	// static class DefaultValueAnnotations2 {
	// 	@SerdeDefault(provider = "defaultServers", cls = DefaultValueProviders.class, whenValue = WhenValue.IS_MISSING)
	// 	List<String> servers;

	// 	@SerdeDefault(provider = "defaultUsers", cls = DefaultValueProviders.class, whenValue = {WhenValue.IS_MISSING, WhenValue.IS_NULL, WhenValue.IS_EMPTY})
	// 	List<String> users;

	// 	@SerdeDefault(provider = "defaultHostSupplier", cls = DefaultValueProviders.class)
	// 	String host;
	// }

	// static class DefaultValueProviders {
	// 	static List<String> defaultServers() {
	// 		return List.of("example.org");
	// 	}

	// 	static List<String> defaultUsers() {
	// 		return List.of("default-user");
	// 	}

	// 	private static final Supplier<String> defaultHostSupplier = () -> "default-host";
	// }
}
