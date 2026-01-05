import re.neotamia.nightconfig.core.Config;
import re.neotamia.nightconfig.core.serde.*;

import java.lang.reflect.Type;

/**
 * Example showing how to use {@link SerdeContext} for type-aware get/set
 * operations.
 * <p>
 * SerdeContext allows you to attach TypeAdapters to a Config, enabling
 * automatic
 * serialization and deserialization when using {@code setTyped()} and
 * {@code getTyped()}.
 * <p>
 * This is particularly useful when you want to use custom types directly with
 * Config
 * without manually converting them to/from primitive types.
 */
public class SerdeContextExample {

    public static void main(String[] args) {
        System.out.println("=== SerdeContext Example ===\n");

        // Create a SerdeContext with our custom TypeAdapter
        SerdeContext ctx = SerdeContext.builder()
                .withTypeAdapter(new ResourceLocationTypeAdapter())
                .build();

        // Create a config and attach the context
        Config config = Config.inMemory();
        config.setSerdeContext(ctx);

        System.out.println("1. Setting values using setTyped():");

        // Use setTyped to automatically serialize custom types
        ResourceLocation stoneBlock = new ResourceLocation("minecraft", "stone");
        ResourceLocation ironOre = new ResourceLocation("forge", "iron_ore");
        ResourceLocation myItem = new ResourceLocation("mymod", "super_sword");

        config.setTyped("blocks.floor", stoneBlock);
        config.setTyped("blocks.ore", ironOre);
        config.setTyped("items.weapon", myItem);

        System.out.println("   Set blocks.floor = " + stoneBlock);
        System.out.println("   Set blocks.ore = " + ironOre);
        System.out.println("   Set items.weapon = " + myItem);

        // Show what's actually stored (raw String values)
        System.out.println("\n2. Raw values stored in config:");
        System.out.println("   blocks.floor (raw) = \"" + config.get("blocks.floor") + "\"");
        System.out.println("   blocks.ore (raw) = \"" + config.get("blocks.ore") + "\"");
        System.out.println("   items.weapon (raw) = \"" + config.get("items.weapon") + "\"");

        // Use getTyped to automatically deserialize back to custom types
        System.out.println("\n3. Getting values using getTyped():");
        ResourceLocation retrievedFloor = config.getTyped("blocks.floor", ResourceLocation.class);
        ResourceLocation retrievedOre = config.getTyped("blocks.ore", ResourceLocation.class);
        ResourceLocation retrievedWeapon = config.getTyped("items.weapon", ResourceLocation.class);

        System.out.println("   blocks.floor = " + retrievedFloor);
        System.out.println("   blocks.ore = " + retrievedOre);
        System.out.println("   items.weapon = " + retrievedWeapon);

        // Use getTypedOrElse for optional values
        System.out.println("\n4. Using getTypedOrElse() with defaults:");
        ResourceLocation defaultLoc = new ResourceLocation("minecraft", "air");
        ResourceLocation missing = config.getTypedOrElse("blocks.missing", ResourceLocation.class, defaultLoc);
        System.out.println("   blocks.missing (not set) = " + missing + " (default)");

        // Verify round-trip
        System.out.println("\n5. Round-trip verification:");
        System.out.println("   floor equals original: " + stoneBlock.equals(retrievedFloor));
        System.out.println("   ore equals original: " + ironOre.equals(retrievedOre));
        System.out.println("   weapon equals original: " + myItem.equals(retrievedWeapon));

        // You can also register adapters at runtime
        System.out.println("\n6. Registering TypeAdapter at runtime:");
        ctx.registerTypeAdapter(new ColorTypeAdapter());
        config.setTyped("ui.background", new Color(255, 128, 0)); // Orange
        Color bgColor = config.getTyped("ui.background", Color.class);
        System.out.println("   ui.background = " + bgColor);
    }

    // ============ Custom Types ============

    /**
     * Example custom type: ResourceLocation (similar to Minecraft's
     * ResourceLocation).
     * Format: "namespace:path"
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
     * Example custom type: Color.
     * Format: "r,g,b"
     */
    static class Color {
        private final int r, g, b;

        public Color(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        @Override
        public String toString() {
            return "Color(" + r + ", " + g + ", " + b + ")";
        }
    }

    // ============ TypeAdapters ============

    /**
     * TypeAdapter that converts ResourceLocation to/from String.
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

    /**
     * TypeAdapter that converts Color to/from String.
     */
    static class ColorTypeAdapter implements TypeAdapter<Color, String> {
        @Override
        public boolean canHandle(Type type) {
            return type == Color.class;
        }

        @Override
        public String serialize(Color value, Type type, SerializerContext ctx) {
            return value.r + "," + value.g + "," + value.b;
        }

        @Override
        public Color deserialize(String value, Type type, DeserializerContext ctx) {
            String[] parts = value.split(",");
            return new Color(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]));
        }
    }
}
