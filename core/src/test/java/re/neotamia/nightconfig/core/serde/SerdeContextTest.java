package re.neotamia.nightconfig.core.serde;

import org.junit.jupiter.api.Test;
import re.neotamia.nightconfig.core.Config;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SerdeContext and typed get/set operations with TypeAdapters.
 */
public class SerdeContextTest {

    // ============ Test Data Classes ============

    /**
     * Simple class to test TypeAdapter functionality.
     * Similar to Minecraft's ResourceLocation.
     */
    static class ResourceLocation {
        private final String namespace;
        private final String path;

        public ResourceLocation(String namespace, String path) {
            this.namespace = namespace;
            this.path = path;
        }

        public ResourceLocation(String combined) {
            int colonIndex = combined.indexOf(':');
            if (colonIndex >= 0) {
                this.namespace = combined.substring(0, colonIndex);
                this.path = combined.substring(colonIndex + 1);
            } else {
                this.namespace = "minecraft";
                this.path = combined;
            }
        }

        public String getNamespace() {
            return namespace;
        }

        public String getPath() {
            return path;
        }

        @Override
        public String toString() {
            return namespace + ":" + path;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ResourceLocation other) {
                return namespace.equals(other.namespace) && path.equals(other.path);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return namespace.hashCode() * 31 + path.hashCode();
        }
    }

    /**
     * TypeAdapter for ResourceLocation that converts to/from String.
     */
    static class ResourceLocationTypeAdapter implements TypeAdapter<ResourceLocation, String> {
        @Override
        public boolean canHandle(Type type) {
            return type == ResourceLocation.class;
        }

        @Override
        public String serialize(ResourceLocation value, Type type, SerializerContext ctx) {
            return value.getNamespace() + ":" + value.getPath();
        }

        @Override
        public ResourceLocation deserialize(String value, Type type, DeserializerContext ctx) {
            return new ResourceLocation(value);
        }
    }

    // ============ Tests ============

    @Test
    public void testSerdeContextBuilder() {
        SerdeContext ctx = SerdeContext.builder()
                .withTypeAdapter(new ResourceLocationTypeAdapter())
                .build();

        assertNotNull(ctx);
        assertNotNull(ctx.getSerializer());
        assertNotNull(ctx.getDeserializer());
    }

    @Test
    public void testSetTypedAndGetTyped() {
        // Create context with ResourceLocation adapter
        SerdeContext ctx = SerdeContext.builder()
                .withTypeAdapter(new ResourceLocationTypeAdapter())
                .build();

        // Create config and attach context
        Config config = Config.inMemory();
        config.setSerdeContext(ctx);

        // Test setTyped
        ResourceLocation location = new ResourceLocation("minecraft", "stone");
        config.setTyped("block", location);

        // Verify the raw value is a String (serialized form)
        String rawValue = config.get("block");
        assertEquals("minecraft:stone", rawValue);

        // Test getTyped
        ResourceLocation retrieved = config.getTyped("block", ResourceLocation.class);
        assertNotNull(retrieved);
        assertEquals("minecraft", retrieved.getNamespace());
        assertEquals("stone", retrieved.getPath());
        assertEquals(location, retrieved);
    }

    @Test
    public void testGetTypedOrElse() {
        SerdeContext ctx = SerdeContext.builder()
                .withTypeAdapter(new ResourceLocationTypeAdapter())
                .build();

        Config config = Config.inMemory();
        config.setSerdeContext(ctx);

        // Test with non-existent key
        ResourceLocation defaultLoc = new ResourceLocation("minecraft", "air");
        ResourceLocation result = config.getTypedOrElse("missing", ResourceLocation.class, defaultLoc);
        assertEquals(defaultLoc, result);

        // Test with existing key
        config.setTyped("block", new ResourceLocation("minecraft", "stone"));
        ResourceLocation retrieved = config.getTypedOrElse("block", ResourceLocation.class, defaultLoc);
        assertEquals(new ResourceLocation("minecraft", "stone"), retrieved);
    }

    @Test
    public void testNoSerdeContextThrowsException() {
        Config config = Config.inMemory();
        // Don't attach any context

        // setTyped should throw
        assertThrows(IllegalStateException.class, () -> {
            config.setTyped("test", "value");
        });

        // getTyped should throw
        assertThrows(IllegalStateException.class, () -> {
            config.getTyped("test", String.class);
        });
    }

    @Test
    public void testRegisterTypeAdapterAtRuntime() {
        SerdeContext ctx = SerdeContext.builder().build();
        Config config = Config.inMemory();
        config.setSerdeContext(ctx);

        // Register adapter at runtime
        ctx.registerTypeAdapter(new ResourceLocationTypeAdapter());

        // Now it should work
        ResourceLocation location = new ResourceLocation("forge", "iron_ore");
        config.setTyped("ore", location);

        ResourceLocation retrieved = config.getTyped("ore", ResourceLocation.class);
        assertEquals(location, retrieved);
    }

    @Test
    public void testRoundTrip() {
        SerdeContext ctx = SerdeContext.builder()
                .withTypeAdapter(new ResourceLocationTypeAdapter())
                .build();

        Config config = Config.inMemory();
        config.setSerdeContext(ctx);

        // Set multiple values
        config.setTyped("block1", new ResourceLocation("minecraft", "stone"));
        config.setTyped("block2", new ResourceLocation("forge", "copper_ore"));
        config.setTyped("item", new ResourceLocation("custom", "my_item"));

        // Retrieve and verify
        assertEquals(new ResourceLocation("minecraft", "stone"),
                config.getTyped("block1", ResourceLocation.class));
        assertEquals(new ResourceLocation("forge", "copper_ore"),
                config.getTyped("block2", ResourceLocation.class));
        assertEquals(new ResourceLocation("custom", "my_item"),
                config.getTyped("item", ResourceLocation.class));
    }

    @Test
    public void testPrimitiveTypesStillWork() {
        SerdeContext ctx = SerdeContext.builder().build();

        Config config = Config.inMemory();
        config.setSerdeContext(ctx);

        // Primitives should still work without special adapters
        config.setTyped("string", "hello");
        config.setTyped("number", 42);
        config.setTyped("bool", true);

        assertEquals("hello", config.getTyped("string", String.class));
        assertEquals(42, config.getTyped("number", Integer.class));
        assertEquals(true, config.getTyped("bool", Boolean.class));
    }
}
