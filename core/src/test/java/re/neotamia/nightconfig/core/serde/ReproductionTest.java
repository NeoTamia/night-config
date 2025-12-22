package re.neotamia.nightconfig.core.serde;

import org.junit.jupiter.api.Test;
import re.neotamia.nightconfig.core.Config;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReproductionTest {
    public static class FloatConfig {
        float f;
        Float wrappedF;
    }

    public static class OtherNumbersConfig {
        int i;
        long l;
        byte b;
        short s;
    }

    @Test
    public void testDoubleToFloat() {
        Config config = Config.inMemory();
        config.set("f", 0.5); // Double by default in many cases, here we force it
        config.set("wrappedF", 0.5);

        ObjectDeserializer deserializer = ObjectDeserializer.standard();
        FloatConfig floatConfig = deserializer.deserializeFields(config, FloatConfig::new);

        assertEquals(0.5f, floatConfig.f);
        assertEquals(0.5f, floatConfig.wrappedF);
    }

    @Test
    public void testDoubleToOtherNumbers() {
        Config config = Config.inMemory();
        config.set("i", 42.0);
        config.set("l", 100.0);
        config.set("b", 10.0);
        config.set("s", 20.0);

        ObjectDeserializer deserializer = ObjectDeserializer.standard();
        OtherNumbersConfig numberConfig = deserializer.deserializeFields(config, OtherNumbersConfig::new);

        assertEquals(42, numberConfig.i);
        assertEquals(100L, numberConfig.l);
        assertEquals((byte)10, numberConfig.b);
        assertEquals((short)20, numberConfig.s);
    }
}
