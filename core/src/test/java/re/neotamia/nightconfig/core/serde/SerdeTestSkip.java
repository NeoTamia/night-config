package re.neotamia.nightconfig.core.serde;

import org.junit.jupiter.api.Test;
import re.neotamia.nightconfig.core.CommentedConfig;
import re.neotamia.nightconfig.core.Config;
import re.neotamia.nightconfig.core.serde.annotations.SerdeSkip;
import re.neotamia.nightconfig.core.serde.annotations.SerdeSkip.SkipIf;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public final class SerdeTestSkip {
    private CommentedConfig serialize(Object o) {
        return ObjectSerializer.builder().build().serializeFields(o, CommentedConfig::inMemory);
    }

    private <T> T deserialize(Config config, Supplier<T> supplier) {
        T instance = supplier.get();
        ObjectDeserializer.builder().build().deserializeFields(config, instance);
        return instance;
    }

    static class SkipAlways {
        @SerdeSkip(SkipIf.ALWAYS)
        String name = "test";

        int value = 42;
    }

    @Test
    public void skipAlways() {
        var obj = new SkipAlways();
        var serialized = serialize(obj);

        // Le champ 'name' devrait être ignoré lors de la sérialisation
        assertFalse(serialized.contains("name"));
        assertTrue(serialized.contains("value"));
        assertEquals(42, serialized.getInt("value"));

        // Lors de la désérialisation, le champ 'name' devrait conserver sa valeur par défaut
        var config = Config.inMemory();
        config.set("name", "from_config");
        config.set("value", 100);

        var deserialized = deserialize(config, SkipAlways::new);
        assertEquals("test", deserialized.name); // Valeur par défaut conservée
        assertEquals(100, deserialized.value);
    }

    static class SkipIfNull {
        @SerdeSkip(SkipIf.IS_NULL)
        String name;

        @SerdeSkip(SkipIf.IS_NULL)
        String description = "default";
    }

    @Test
    public void skipIfNull() {
        var obj = new SkipIfNull();
        obj.name = null;
        obj.description = "not null";

        var serialized = serialize(obj);

        // Le champ 'name' (null) devrait être ignoré
        assertFalse(serialized.contains("name"));
        // Le champ 'description' (non-null) devrait être inclus
        assertTrue(serialized.contains("description"));
        assertEquals("not null", serialized.get("description"));

        // Test avec valeur non-null
        obj.name = "test name";
        serialized = serialize(obj);
        assertTrue(serialized.contains("name"));
        assertEquals("test name", serialized.get("name"));
    }

    static class SkipIfEmpty {
        @SerdeSkip(SkipIf.IS_EMPTY)
        String name;

        @SerdeSkip(SkipIf.IS_EMPTY)
        String description = "default";
    }

    @Test
    public void skipIfEmpty() {
        var obj = new SkipIfEmpty();
        obj.name = "";
        obj.description = "not empty";

        var serialized = serialize(obj);

        // Le champ 'name' (vide) devrait être ignoré
        assertFalse(serialized.contains("name"));
        // Le champ 'description' (non-vide) devrait être inclus
        assertTrue(serialized.contains("description"));
        assertEquals("not empty", serialized.get("description"));

        // Test avec valeur non-vide
        obj.name = "test name";
        serialized = serialize(obj);
        assertTrue(serialized.contains("name"));
        assertEquals("test name", serialized.get("name"));
    }

    static class SkipMultipleConditions {
        @SerdeSkip({SkipIf.IS_NULL, SkipIf.IS_EMPTY})
        String name;

        int value = 42;
    }

    @Test
    public void skipMultipleConditions() {
        var obj = new SkipMultipleConditions();

        // Test avec null
        obj.name = null;
        var serialized = serialize(obj);
        assertFalse(serialized.contains("name"));
        assertTrue(serialized.contains("value"));

        // Test avec chaîne vide
        obj.name = "";
        serialized = serialize(obj);
        assertFalse(serialized.contains("name"));

        // Test avec valeur valide
        obj.name = "valid";
        serialized = serialize(obj);
        assertTrue(serialized.contains("name"));
        assertEquals("valid", serialized.get("name"));
    }

    static class SkipCustomInObject {
        @SerdeSkip(value = SkipIf.CUSTOM, customCheck = "shouldSkipName")
        String name = "test";

        @SuppressWarnings("unused")
        boolean shouldSkipName(String obj) {
            return "skip_me".equals(obj);
        }
    }

    @Test
    public void skipCustomInObject() {
        var obj = new SkipCustomInObject();

        // Test avec valeur qui ne doit pas être ignorée
        obj.name = "keep_me";
        var serialized = serialize(obj);
        assertTrue(serialized.contains("name"));
        assertEquals("keep_me", serialized.get("name"));

        // Test avec valeur qui doit être ignorée
        obj.name = "skip_me";
        serialized = serialize(obj);
        assertFalse(serialized.contains("name"));
    }

    static class SkipCustomWithPredicate {
        @SerdeSkip(value = SkipIf.CUSTOM, customCheck = "skipPredicate")
        String name = "test";

        @SuppressWarnings("unused")
        private final transient Predicate<Object> skipPredicate = obj -> obj != null && obj.toString().startsWith("skip_");
    }

    @Test
    public void skipCustomWithPredicate() {
        var obj = new SkipCustomWithPredicate();

        // Test avec valeur qui ne doit pas être ignorée
        obj.name = "keep_this";
        var serialized = serialize(obj);
        assertTrue(serialized.contains("name"));
        assertEquals("keep_this", serialized.get("name"));

        // Test avec valeur qui doit être ignorée
        obj.name = "skip_this";
        serialized = serialize(obj);
        assertFalse(serialized.contains("name"));
    }

    static class SkipPredicates {
        @SuppressWarnings("unused")
        public static boolean skipIfLong(String obj) {
            return obj != null && obj.length() > 5;
        }

        @SuppressWarnings("unused")
        public static final Predicate<Object> skipField = obj -> obj != null && obj.equals("SKIP");
    }

    static class SkipCustomExternalClass {
        @SerdeSkip(value = SkipIf.CUSTOM, customClass = SkipPredicates.class, customCheck = "skipIfLong")
        String name = "test";

        @SerdeSkip(value = SkipIf.CUSTOM, customClass = SkipPredicates.class, customCheck = "skipField")
        String description = "DESC";
    }

    @Test
    public void skipCustomExternalClass() {
        var obj = new SkipCustomExternalClass();

        // Test avec nom court (ne doit pas être ignoré)
        obj.name = "short";
        obj.description = "KEEP";
        var serialized = serialize(obj);
        assertTrue(serialized.contains("name"));
        assertTrue(serialized.contains("description"));
        assertEquals("short", serialized.get("name"));
        assertEquals("KEEP", serialized.get("description"));

        // Test avec nom long (doit être ignoré)
        obj.name = "very_long_name";
        obj.description = "SKIP";
        serialized = serialize(obj);
        assertFalse(serialized.contains("name"));
        assertFalse(serialized.contains("description"));
    }

    static class SkipCustomWrong {
        @SerdeSkip(SkipIf.CUSTOM) // erreur: paramètres manquants
        @SuppressWarnings("unused")
        String name;
    }

    @Test
    public void customWrong() {
        assertThrowsExactly(SerdeException.class, () -> serialize(new SkipCustomWrong()));
    }

    // ====== Additional Deserialization Skip Tests ======

    static class SkipIfNullDeserialization {
        @SerdeSkip(SkipIf.IS_NULL)
        String name = "default_name";

        @SerdeSkip(SkipIf.IS_NULL)
        Integer age = 25;
    }

    @Test
    public void skipIfNullDeserialization() {
        // Test avec valeur null dans la config (devrait être ignorée)
        var configWithNull = Config.inMemory();
        configWithNull.set("name", null);
        configWithNull.set("age", 30);

        var deserialized = deserialize(configWithNull, SkipIfNullDeserialization::new);
        assertEquals("default_name", deserialized.name); // Ignoré car null, garde la valeur par défaut
        assertEquals(30, deserialized.age); // Non null, doit être désérialisé

        // Test avec valeurs non-null
        var configWithValues = Config.inMemory();
        configWithValues.set("name", "john");
        configWithValues.set("age", 40);

        deserialized = deserialize(configWithValues, SkipIfNullDeserialization::new);
        assertEquals("john", deserialized.name);
        assertEquals(40, deserialized.age);
    }

    static class SkipIfEmptyDeserialization {
        @SerdeSkip(SkipIf.IS_EMPTY)
        String name = "default_name";

        @SerdeSkip(SkipIf.IS_EMPTY)
        java.util.List<String> items = List.of("default");
    }

    @Test
    public void skipIfEmptyDeserialization() {
        // Test avec valeurs vides dans la config (devraient être ignorées)
        var configWithEmpty = Config.inMemory();
        configWithEmpty.set("name", "");
        configWithEmpty.set("items", java.util.Collections.emptyList());

        var deserialized = deserialize(configWithEmpty, SkipIfEmptyDeserialization::new);
        assertEquals("default_name", deserialized.name); // Ignoré car vide, garde la valeur par défaut
        assertEquals(1, deserialized.items.size()); // Ignoré car vide, garde la valeur par défaut
        assertEquals("default", deserialized.items.getFirst());

        // Test avec valeurs non-vides
        var configWithValues = Config.inMemory();
        configWithValues.set("name", "john");
        configWithValues.set("items", java.util.Arrays.asList("item1", "item2"));

        deserialized = deserialize(configWithValues, SkipIfEmptyDeserialization::new);
        assertEquals("john", deserialized.name);
        assertEquals(2, deserialized.items.size());
        assertEquals("item1", deserialized.items.get(0));
        assertEquals("item2", deserialized.items.get(1));
    }

    static class SkipCustomDeserializationPredicates {
        @SuppressWarnings("unused")
        public static boolean skipIfNegative(Object obj) {
            return obj instanceof Number && ((Number) obj).intValue() < 0;
        }

        @SuppressWarnings("unused")
        public static final Predicate<Object> skipIfContainsTest = obj -> 
            obj != null && obj.toString().toLowerCase().contains("test");
    }

    static class SkipCustomDeserialization {
        @SerdeSkip(value = SkipIf.CUSTOM, customClass = SkipCustomDeserializationPredicates.class, customCheck = "skipIfNegative")
        int score = 100;

        @SerdeSkip(value = SkipIf.CUSTOM, customClass = SkipCustomDeserializationPredicates.class, customCheck = "skipIfContainsTest")
        String message = "default_message";
    }

    @Test
    public void skipCustomDeserialization() {
        // Test avec valeurs qui doivent être ignorées
        var configWithSkippedValues = Config.inMemory();
        configWithSkippedValues.set("score", -10); // Négatif, doit être ignoré
        configWithSkippedValues.set("message", "This is a test message"); // Contient "test", doit être ignoré

        var deserialized = deserialize(configWithSkippedValues, SkipCustomDeserialization::new);
        assertEquals(100, deserialized.score); // Valeur par défaut conservée
        assertEquals("default_message", deserialized.message); // Valeur par défaut conservée

        // Test avec valeurs qui ne doivent pas être ignorées
        var configWithValidValues = Config.inMemory();
        configWithValidValues.set("score", 85); // Positif, ne doit pas être ignoré
        configWithValidValues.set("message", "Hello world"); // Ne contient pas "test", ne doit pas être ignoré

        deserialized = deserialize(configWithValidValues, SkipCustomDeserialization::new);
        assertEquals(85, deserialized.score);
        assertEquals("Hello world", deserialized.message);
    }

    static class SkipMultipleConditionsDeserialization {
        @SerdeSkip({SkipIf.IS_NULL, SkipIf.IS_EMPTY})
        String name = "default_name";

        @SerdeSkip({SkipIf.IS_NULL, SkipIf.IS_EMPTY})
        java.util.List<Integer> numbers = java.util.Arrays.asList(1, 2, 3);
    }

    @Test
    public void skipMultipleConditionsDeserialization() {
        // Test avec valeur null
        var configWithNull = Config.inMemory();
        configWithNull.set("name", null);
        configWithNull.set("numbers", java.util.Arrays.asList(10, 20));

        var deserialized = deserialize(configWithNull, SkipMultipleConditionsDeserialization::new);
        assertEquals("default_name", deserialized.name); // Null ignoré
        assertEquals(2, deserialized.numbers.size()); // Pas null, désérialisé
        assertEquals(10, deserialized.numbers.get(0));

        // Test avec valeur vide
        var configWithEmpty = Config.inMemory();
        configWithEmpty.set("name", "");
        configWithEmpty.set("numbers", java.util.Collections.emptyList());

        deserialized = deserialize(configWithEmpty, SkipMultipleConditionsDeserialization::new);
        assertEquals("default_name", deserialized.name); // Vide ignoré
        assertEquals(3, deserialized.numbers.size()); // Vide ignoré, valeur par défaut
        assertEquals(1, deserialized.numbers.get(0));

        // Test avec valeurs valides
        var configWithValid = Config.inMemory();
        configWithValid.set("name", "john");
        configWithValid.set("numbers", java.util.Arrays.asList(100, 200, 300));

        deserialized = deserialize(configWithValid, SkipMultipleConditionsDeserialization::new);
        assertEquals("john", deserialized.name);
        assertEquals(3, deserialized.numbers.size());
        assertEquals(100, deserialized.numbers.get(0));
    }

    static class SkipInternalCustomDeserialization {
        @SerdeSkip(value = SkipIf.CUSTOM, customCheck = "shouldSkipDuringDeserialization")
        String data = "default_data";

        @SuppressWarnings("unused")
        boolean shouldSkipDuringDeserialization(Object obj) {
            // Skip si la valeur est "SKIP_ME" ou commence par "IGNORE_"
            return obj != null && (obj.equals("SKIP_ME") || obj.toString().startsWith("IGNORE_"));
        }
    }

    @Test
    public void skipInternalCustomDeserialization() {
        // Test avec valeur qui doit être ignorée
        var configWithSkippedValue = Config.inMemory();
        configWithSkippedValue.set("data", "SKIP_ME");

        var deserialized = deserialize(configWithSkippedValue, SkipInternalCustomDeserialization::new);
        assertEquals("default_data", deserialized.data); // Valeur ignorée

        // Test avec autre valeur qui doit être ignorée
        var configWithIgnoredValue = Config.inMemory();
        configWithIgnoredValue.set("data", "IGNORE_THIS_VALUE");

        deserialized = deserialize(configWithIgnoredValue, SkipInternalCustomDeserialization::new);
        assertEquals("default_data", deserialized.data); // Valeur ignorée

        // Test avec valeur qui ne doit pas être ignorée
        var configWithValidValue = Config.inMemory();
        configWithValidValue.set("data", "KEEP_THIS_VALUE");

        deserialized = deserialize(configWithValidValue, SkipInternalCustomDeserialization::new);
        assertEquals("KEEP_THIS_VALUE", deserialized.data); // Valeur désérialisée
    }
}
