package re.neotamia.nightconfig.core.serde;

import org.junit.jupiter.api.Test;
import re.neotamia.nightconfig.core.Config;
import re.neotamia.nightconfig.core.serde.annotations.SerdeKey;

import static org.junit.jupiter.api.Assertions.*;

public final class NamingStrategyTest {

    static class TestClass {
        public String userName = "john";
        public int maxRetries = 5;
        public boolean isEnabled = true;
        public String HTTPMethod = "GET";
        public String XMLParser = "default";
    }

    @Test
    public void testIdentityStrategy() {
        NamingStrategy strategy = NamingStrategy.IDENTITY;

        assertEquals("userName", strategy.transformName("userName"));
        assertEquals("maxRetries", strategy.transformName("maxRetries"));
        assertEquals("isEnabled", strategy.transformName("isEnabled"));
        assertEquals("HTTPMethod", strategy.transformName("HTTPMethod"));
    }

    @Test
    public void testSnakeCaseStrategy() {
        NamingStrategy strategy = NamingStrategy.SNAKE_CASE;

        assertEquals("user_name", strategy.transformName("userName"));
        assertEquals("max_retries", strategy.transformName("maxRetries"));
        assertEquals("is_enabled", strategy.transformName("isEnabled"));
        assertEquals("http_method", strategy.transformName("httpMethod"));
        assertEquals("xml_parser", strategy.transformName("xmlParser"));
        assertEquals("simple", strategy.transformName("simple"));
        assertEquals("", strategy.transformName(""));
        assertNull(strategy.transformName(null));
    }

    @Test
    public void testKebabCaseStrategy() {
        NamingStrategy strategy = NamingStrategy.KEBAB_CASE;

        assertEquals("user-name", strategy.transformName("userName"));
        assertEquals("max-retries", strategy.transformName("maxRetries"));
        assertEquals("is-enabled", strategy.transformName("isEnabled"));
        assertEquals("httpmethod", strategy.transformName("HTTPMethod"));
        assertEquals("xmlparser", strategy.transformName("XMLParser"));
        assertEquals("simple", strategy.transformName("simple"));
        assertEquals("", strategy.transformName(""));
        assertNull(strategy.transformName(null));
    }

    @Test
    public void testCamelCaseStrategy() {
        NamingStrategy strategy = NamingStrategy.CAMEL_CASE;

        assertEquals("userName", strategy.transformName("userName"));
        assertEquals("userName", strategy.transformName("UserName"));
        assertEquals("maxRetries", strategy.transformName("MaxRetries"));
        assertEquals("isEnabled", strategy.transformName("IsEnabled"));
        assertEquals("httpMethod", strategy.transformName("HTTPMethod"));
        assertEquals("simple", strategy.transformName("simple"));
        assertEquals("simple", strategy.transformName("Simple"));
        assertEquals("", strategy.transformName(""));
        assertNull(strategy.transformName(null));
    }

    @Test
    public void testPascalCaseStrategy() {
        NamingStrategy strategy = NamingStrategy.PASCAL_CASE;

        assertEquals("UserName", strategy.transformName("userName"));
        assertEquals("UserName", strategy.transformName("UserName"));
        assertEquals("MaxRetries", strategy.transformName("maxRetries"));
        assertEquals("IsEnabled", strategy.transformName("isEnabled"));
        assertEquals("HTTPMethod", strategy.transformName("HTTPMethod"));
        assertEquals("Simple", strategy.transformName("simple"));
        assertEquals("Simple", strategy.transformName("Simple"));
        assertEquals("", strategy.transformName(""));
        assertNull(strategy.transformName(null));
    }

    @Test
    public void testSerializationWithSnakeCase() {
        var serializer = ObjectSerializer.builder().withNamingStrategy(NamingStrategy.SNAKE_CASE).build();

        var obj = new TestClass();
        var config = serializer.serializeFields(obj, Config::inMemory);

        // Vérifier que les noms de champs sont transformés en snake_case
        assertTrue(config.contains("user_name"));
        assertTrue(config.contains("max_retries"));
        assertTrue(config.contains("is_enabled"));
        assertTrue(config.contains("httpmethod"));

        // Vérifier que les valeurs sont correctes
        assertEquals("john", config.get("user_name"));
        assertEquals(5, config.getInt("max_retries"));
        assertEquals(true, config.get("is_enabled"));
        assertEquals("GET", config.get("httpmethod"));

        // Vérifier que les noms originaux ne sont pas présents
        assertFalse(config.contains("userName"));
        assertFalse(config.contains("maxRetries"));
        assertFalse(config.contains("isEnabled"));
        assertFalse(config.contains("HTTPMethod"));
    }

    @Test
    public void testSerializationWithKebabCase() {
        var serializer = ObjectSerializer.builder().withNamingStrategy(NamingStrategy.KEBAB_CASE).build();

        var obj = new TestClass();
        var config = serializer.serializeFields(obj, Config::inMemory);

        // Vérifier que les noms de champs sont transformés en kebab-case
        assertTrue(config.contains("user-name"));
        assertTrue(config.contains("max-retries"));
        assertTrue(config.contains("is-enabled"));
        assertTrue(config.contains("httpmethod"));

        // Vérifier que les valeurs sont correctes
        assertEquals("john", config.get("user-name"));
        assertEquals(5, config.getInt("max-retries"));
        assertEquals(true, config.get("is-enabled"));
        assertEquals("GET", config.get("httpmethod"));
    }

    @Test
    public void testDeserializationWithSnakeCase() {
        var deserializer = ObjectDeserializer.builder()
                .withNamingStrategy(NamingStrategy.SNAKE_CASE)
                .build();

        // Créer une config avec des clés en snake_case
        var config = Config.inMemory();
        config.set("user_name", "jane");
        config.set("max_retries", 10);
        config.set("is_enabled", false);
        config.set("httpmethod", "POST");
        config.set("xmlparser", "default");

        var obj = deserializer.deserializeFields(config, TestClass::new);

        // Vérifier que la désérialisation a fonctionné
        assertEquals("jane", obj.userName);
        assertEquals(10, obj.maxRetries);
        assertFalse(obj.isEnabled);
        assertEquals("POST", obj.HTTPMethod);
    }

    @Test
    public void testDeserializationWithKebabCase() {
        var deserializer = ObjectDeserializer.builder().withNamingStrategy(NamingStrategy.KEBAB_CASE).build();
        // Créer une config avec des clés en kebab-case
        var config = Config.inMemory();
        config.set("user-name", "bob");
        config.set("max-retries", 15);
        config.set("is-enabled", true);
        config.set("httpmethod", "PUT");
        config.set("xmlparser", "default");

        var obj = deserializer.deserializeFields(config, TestClass::new);

        // Vérifier que la désérialisation a fonctionné
        assertEquals("bob", obj.userName);
        assertEquals(15, obj.maxRetries);
        assertTrue(obj.isEnabled);
        assertEquals("PUT", obj.HTTPMethod);
    }

    @Test
    public void testRoundTripWithSnakeCase() {
        var serializer = ObjectSerializer.builder().withNamingStrategy(NamingStrategy.SNAKE_CASE).build();
        var deserializer = ObjectDeserializer.builder().withNamingStrategy(NamingStrategy.SNAKE_CASE).build();

        // Objet original
        var original = new TestClass();
        original.userName = "alice";
        original.maxRetries = 20;
        original.isEnabled = false;
        original.HTTPMethod = "DELETE";

        // Sérialisation
        var config = serializer.serializeFields(original, Config::inMemory);

        // Désérialisation
        var deserialized = deserializer.deserializeFields(config, TestClass::new);

        // Vérifier que l'objet est identique
        assertEquals(original.userName, deserialized.userName);
        assertEquals(original.maxRetries, deserialized.maxRetries);
        assertEquals(original.isEnabled, deserialized.isEnabled);
        assertEquals(original.HTTPMethod, deserialized.HTTPMethod);
    }

    static class MixedAnnotationsClass {
        @SerdeKey("custom_key")
        public String userName = "test";

        public String maxRetries = "retry";
    }

    @Test
    public void testNamingStrategyWithExplicitKeys() {
        var serializer = ObjectSerializer.builder().withNamingStrategy(NamingStrategy.SNAKE_CASE).build();

        var obj = new MixedAnnotationsClass();
        var config = serializer.serializeFields(obj, Config::inMemory);

        // Le champ avec @SerdeKey doit utiliser la clé explicite
        assertTrue(config.contains("custom_key"));
        assertEquals("test", config.get("custom_key"));

        // Le champ sans annotation doit utiliser la naming strategy
        assertTrue(config.contains("max_retries"));
        assertEquals("retry", config.get("max_retries"));

        // Vérifier que les noms originaux ne sont pas présents
        assertFalse(config.contains("userName"));
        assertFalse(config.contains("maxRetries"));
    }

    @Test
    public void testDeserializationWithExplicitKeys() {
        var deserializer = ObjectDeserializer.builder().withNamingStrategy(NamingStrategy.SNAKE_CASE).build();

        var config = Config.inMemory();
        config.set("custom_key", "value1");
        config.set("max_retries", "value2");

        var obj = deserializer.deserializeFields(config, MixedAnnotationsClass::new);

        assertEquals("value1", obj.userName);
        assertEquals("value2", obj.maxRetries);
    }
}
