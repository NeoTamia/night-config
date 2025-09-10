package re.neotamia.nightconfig.core.serde;

import org.junit.jupiter.api.Test;
import re.neotamia.nightconfig.core.CommentedConfig;
import re.neotamia.nightconfig.core.Config;
import re.neotamia.nightconfig.core.serde.annotations.*;
import re.neotamia.nightconfig.core.serde.annotations.SerdeAssert.AssertThat;
import re.neotamia.nightconfig.core.serde.annotations.SerdeDefault.WhenValue;
import re.neotamia.nightconfig.core.serde.annotations.SerdeSkip.SkipIf;
import re.neotamia.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf.SkipDeIf;
import re.neotamia.nightconfig.core.serde.annotations.SerdeSkipSerializingIf.SkipSerIf;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public final class SerdeTestConfig {
    private CommentedConfig serialize(Object o) {
        return ObjectSerializer.builder().build().serializeFields(o, CommentedConfig::inMemory);
    }

    private <T> T deserialize(Config config, Supplier<T> supplier) {
        T instance = supplier.get();
        ObjectDeserializer.builder().build().deserializeFields(config, instance);
        return instance;
    }

    static class ConfigWithKey {
        @SerdeConfig(key = "custom_name")
        String name = "test";

        int value = 42;
    }

    @Test
    public void configWithKey() {
        var obj = new ConfigWithKey();
        obj.name = "hello";

        var serialized = serialize(obj);

        // Le champ devrait être sérialisé avec la clé personnalisée
        assertFalse(serialized.contains("name"));
        assertTrue(serialized.contains("custom_name"));
        assertEquals("hello", serialized.get("custom_name"));

        // Test de désérialisation
        var config = Config.inMemory();
        config.set("custom_name", "world");
        config.set("value", 100);

        var deserialized = deserialize(config, ConfigWithKey::new);
        assertEquals("world", deserialized.name);
        assertEquals(100, deserialized.value);
    }

    static class ConfigWithComments {
        @SerdeConfig(comments = {
                @SerdeComment("This is the user's name"),
                @SerdeComment("It should be a valid string")
        })
        String name = "test";
    }

    @Test
    public void configWithComments() {
        var obj = new ConfigWithComments();
        obj.name = "John";

        var serialized = serialize(obj);

        // Vérifier que le champ est présent
        assertTrue(serialized.contains("name"));
        assertEquals("John", serialized.get("name"));

        // Vérifier que les commentaires sont présents
        var comments = serialized.getComment("name");
        assertNotNull(comments);
        assertTrue(comments.contains("This is the user's name"));
        assertTrue(comments.contains("It should be a valid string"));
    }

    static class ConfigWithDefaults {
        @SerdeConfig(defaults = @SerdeDefault(provider = "getDefaultName", whenValue = WhenValue.IS_MISSING))
        String name;

        String getDefaultName() {
            return "default_name";
        }
    }

    @Test
    public void configWithDefaults() {
        // Test avec config vide (valeur par défaut appliquée)
        var emptyConfig = Config.inMemory();
        var deserialized = deserialize(emptyConfig, ConfigWithDefaults::new);
        assertEquals("default_name", deserialized.name);

        // Test avec valeur dans la config
        var config = Config.inMemory();
        config.set("name", "custom_name");
        deserialized = deserialize(config, ConfigWithDefaults::new);
        assertEquals("custom_name", deserialized.name);
    }

    static class ConfigWithAsserts {
        @SerdeConfig(asserts = @SerdeAssert(value = AssertThat.NOT_NULL))
        String name;
    }

    @Test
    public void configWithAsserts() {
        // Test avec valeur valide
        var config = Config.inMemory();
        config.set("name", "valid_name");
        var deserialized = deserialize(config, ConfigWithAsserts::new);
        assertEquals("valid_name", deserialized.name);

        // Test avec valeur null (devrait échouer)
        var nullConfig = Config.inMemory();
        nullConfig.set("name", null);

        assertThrowsExactly(SerdeAssertException.class, () -> {
            deserialize(nullConfig, ConfigWithAsserts::new);
        });
    }

    static class ConfigWithSkip {
        @SerdeConfig(skip = @SerdeSkip(SkipIf.IS_NULL))
        String name;

        int value = 42;
    }

    @Test
    public void configWithSkip() {
        var obj = new ConfigWithSkip();
        obj.name = null;

        var serialized = serialize(obj);

        // Le champ null devrait être ignoré
        assertFalse(serialized.contains("name"));
        assertTrue(serialized.contains("value"));

        // Test avec valeur non-null
        obj.name = "test";
        serialized = serialize(obj);
        assertTrue(serialized.contains("name"));
        assertEquals("test", serialized.get("name"));
    }

    static class ConfigWithSkipSerializing {
        @SerdeConfig(skipSerializingIf = @SerdeSkipSerializingIf(SkipSerIf.IS_EMPTY))
        String name = "";

        int value = 42;
    }

    @Test
    public void configWithSkipSerializing() {
        var obj = new ConfigWithSkipSerializing();

        // Le champ vide devrait être ignoré lors de la sérialisation
        var serialized = serialize(obj);
        assertFalse(serialized.contains("name"));
        assertTrue(serialized.contains("value"));

        // Test avec valeur non-vide
        obj.name = "test";
        serialized = serialize(obj);
        assertTrue(serialized.contains("name"));
        assertEquals("test", serialized.get("name"));
    }

    static class ConfigWithSkipDeserializing {
        @SerdeConfig(skipDeserializingIf = @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING))
        String name = "default_value";
    }

    @Test
    public void configWithSkipDeserializing() {
        // Test avec champ manquant (devrait conserver la valeur par défaut)
        var emptyConfig = Config.inMemory();
        var deserialized = deserialize(emptyConfig, ConfigWithSkipDeserializing::new);
        assertEquals("default_value", deserialized.name);

        // Test avec champ présent
        var config = Config.inMemory();
        config.set("name", "from_config");
        deserialized = deserialize(config, ConfigWithSkipDeserializing::new);
        assertEquals("from_config", deserialized.name);
    }

    static class ConfigComplete {
        @SerdeConfig(
                key = "user_name",
                comments = {
                        @SerdeComment("The username for authentication"),
                        @SerdeComment("Must be between 3 and 20 characters")
                },
                defaults = @SerdeDefault(provider = "getDefaultUsername", whenValue = WhenValue.IS_MISSING),
                asserts = @SerdeAssert(value = AssertThat.NOT_NULL),
                skipSerializingIf = @SerdeSkipSerializingIf(SkipSerIf.IS_EMPTY)
        )
        String username;

        @SerdeConfig(
                key = "user_age",
                defaults = @SerdeDefault(provider = "getDefaultAge", whenValue = WhenValue.IS_MISSING),
                asserts = @SerdeAssert(value = AssertThat.CUSTOM, customCheck = "isPositive")
        )
        int age;

        String getDefaultUsername() {
            return "anonymous";
        }

        int getDefaultAge() {
            return 18;
        }

        static boolean isPositive(int age) {
            return age >= 0;
        }
    }

    @Test
    public void configComplete() {
        // Test de sérialisation complète
        var obj = new ConfigComplete();
        obj.username = "john_doe";
        obj.age = 25;

        var serialized = serialize(obj);

        // Vérifier les clés personnalisées
        assertFalse(serialized.contains("username"));
        assertFalse(serialized.contains("age"));
        assertTrue(serialized.contains("user_name"));
        assertTrue(serialized.contains("user_age"));
        assertEquals("john_doe", serialized.get("user_name"));
        assertEquals(25, serialized.getInt("user_age"));

        // Vérifier les commentaires
        var usernameComments = serialized.getComment("user_name");
        assertNotNull(usernameComments);
        assertTrue(usernameComments.contains("The username for authentication"));
        assertTrue(usernameComments.contains("Must be between 3 and 20 characters"));

        // Test de désérialisation avec valeurs par défaut
        var emptyConfig = Config.inMemory();
        var deserialized = deserialize(emptyConfig, ConfigComplete::new);
        assertEquals("anonymous", deserialized.username);
        assertEquals(18, deserialized.age);

        // Test de désérialisation avec valeurs personnalisées
        var config = Config.inMemory();
        config.set("user_name", "jane_doe");
        config.set("user_age", 30);
        deserialized = deserialize(config, ConfigComplete::new);
        assertEquals("jane_doe", deserialized.username);
        assertEquals(30, deserialized.age);

        // Test avec username vide (devrait être ignoré lors de la sérialisation)
        obj.username = "";
        serialized = serialize(obj);
        assertFalse(serialized.contains("user_name"));
        assertTrue(serialized.contains("user_age"));
    }

    @Test
    public void configCompleteAssertions() {
        // Test avec âge négatif (devrait échouer)
        var configWithNegativeAge = Config.inMemory();
        configWithNegativeAge.set("user_name", "test");
        configWithNegativeAge.set("user_age", -5);

        assertThrowsExactly(SerdeAssertException.class, () -> {
            deserialize(configWithNegativeAge, ConfigComplete::new);
        });

        // Test avec username null (devrait échouer)
        var configWithNullUsername = Config.inMemory();
        configWithNullUsername.set("user_name", null);
        configWithNullUsername.set("user_age", 25);

        assertThrowsExactly(SerdeAssertException.class, () -> {
            deserialize(configWithNullUsername, ConfigComplete::new);
        });
    }

    static class ConfigMultipleAnnotations {
        @SerdeConfig(
                key = "servers",
                comments = @SerdeComment("List of available servers"),
                defaults = @SerdeDefault(provider = "getDefaultServers", whenValue = WhenValue.IS_MISSING)
        )
        List<String> serverList;

        List<String> getDefaultServers() {
            return Arrays.asList("localhost:8080", "backup.example.com:8080");
        }
    }

    @Test
    public void configMultipleAnnotations() {
        // Test avec valeurs par défaut
        var emptyConfig = Config.inMemory();
        var deserialized = deserialize(emptyConfig, ConfigMultipleAnnotations::new);
        assertNotNull(deserialized.serverList);
        assertEquals(2, deserialized.serverList.size());
        assertEquals("localhost:8080", deserialized.serverList.get(0));
        assertEquals("backup.example.com:8080", deserialized.serverList.get(1));

        // Test avec valeurs personnalisées
        var config = Config.inMemory();
        config.set("servers", Arrays.asList("server1.com", "server2.com"));
        deserialized = deserialize(config, ConfigMultipleAnnotations::new);
        assertEquals(2, deserialized.serverList.size());
        assertEquals("server1.com", deserialized.serverList.get(0));
        assertEquals("server2.com", deserialized.serverList.get(1));

        // Test de sérialisation
        var obj = new ConfigMultipleAnnotations();
        obj.serverList = Arrays.asList("test1.com", "test2.com");

        var serialized = serialize(obj);
        assertTrue(serialized.contains("servers"));
        var serializedList = serialized.get("servers");
        assertNotNull(serializedList);

        // Vérifier les commentaires
        var comments = serialized.getComment("servers");
        assertNotNull(comments);
        assertTrue(comments.contains("List of available servers"));
    }
}
