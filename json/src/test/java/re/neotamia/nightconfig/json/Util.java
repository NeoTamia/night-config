package re.neotamia.nightconfig.json;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Arrays;

import re.neotamia.nightconfig.core.Config;
import re.neotamia.sharedtests.TestEnum;

public class Util {
    static void checkExample(Config config) {
        assertEquals("This is a string with a lot of characters to escape \n\r\t \\ \" ",
                config.get("string"));
        assertNull(config.get("null"));
        assertEquals(0.123456, config.<Double>get("double"));
        assertEquals(0.123456, config.<Number>get("float"));
        assertEquals(Arrays.asList(
                "a", "b", 3,
                null,
                true,
                false,
                17.5), config.get("list"));
        assertEquals(true, config.get("config.boolean"));
        assertEquals(false, config.get("config.false"));
        assertEquals("value", config.get(List.of("dots.in.key")));
        assertEquals(123456, config.getInt("int"));
        assertEquals(1234567890L, config.getLong("long"));
        assertEquals("A", config.get("enum"));
    }

    static void populateTest(Config config) {
        Config subConfig = config.createSubConfig();
        subConfig.set("string", "test");
        subConfig.set("null value", null);
        subConfig.set("sub", config.createSubConfig());

        List<Config> arrayOfTables = List.of(subConfig, subConfig, subConfig);

        config.set("string", "\"value\"");
        config.set("integer", 2);
        config.set("long", 123456789L);
        config.set("double", 3.1415926535);
        config.set("bool_array", List.of(true, false, true, false));
        config.set("config", subConfig);
        config.set("table_array", arrayOfTables);
        config.set("table_array2", arrayOfTables);
        config.set("enum", TestEnum.A);
    }

    static final String EXPECTED_SERIALIZED_FANCY = "{" + System.lineSeparator() + //
            "\t\"bool_array\": [" + System.lineSeparator() + //
            "\t\ttrue, " + System.lineSeparator() + //
            "\t\tfalse, " + System.lineSeparator() + //
            "\t\ttrue, " + System.lineSeparator() + //
            "\t\tfalse" + System.lineSeparator() + //
            "\t]," + System.lineSeparator() + //
            "\t\"table_array2\": [" + System.lineSeparator() + //
            "\t\t{" + System.lineSeparator() + //
            "\t\t\t\"sub\": {}," + System.lineSeparator() + //
            "\t\t\t\"string\": \"test\"," + System.lineSeparator() + //
            "\t\t\t\"null value\": null" + System.lineSeparator() + //
            "\t\t}, " + System.lineSeparator() + //
            "\t\t{" + System.lineSeparator() + //
            "\t\t\t\"sub\": {}," + System.lineSeparator() + //
            "\t\t\t\"string\": \"test\"," + System.lineSeparator() + //
            "\t\t\t\"null value\": null" + System.lineSeparator() + //
            "\t\t}, " + System.lineSeparator() + //
            "\t\t{" + System.lineSeparator() + //
            "\t\t\t\"sub\": {}," + System.lineSeparator() + //
            "\t\t\t\"string\": \"test\"," + System.lineSeparator() + //
            "\t\t\t\"null value\": null" + System.lineSeparator() + //
            "\t\t}" + System.lineSeparator() + //
            "\t]," + System.lineSeparator() + //
            "\t\"string\": \"\\\"value\\\"\"," + System.lineSeparator() + //
            "\t\"double\": 3.1415926535," + System.lineSeparator() + //
            "\t\"integer\": 2," + System.lineSeparator() + //
            "\t\"table_array\": [" + System.lineSeparator() + //
            "\t\t{" + System.lineSeparator() + //
            "\t\t\t\"sub\": {}," + System.lineSeparator() + //
            "\t\t\t\"string\": \"test\"," + System.lineSeparator() + //
            "\t\t\t\"null value\": null" + System.lineSeparator() + //
            "\t\t}, " + System.lineSeparator() + //
            "\t\t{" + System.lineSeparator() + //
            "\t\t\t\"sub\": {}," + System.lineSeparator() + //
            "\t\t\t\"string\": \"test\"," + System.lineSeparator() + //
            "\t\t\t\"null value\": null" + System.lineSeparator() + //
            "\t\t}, " + System.lineSeparator() + //
            "\t\t{" + System.lineSeparator() + //
            "\t\t\t\"sub\": {}," + System.lineSeparator() + //
            "\t\t\t\"string\": \"test\"," + System.lineSeparator() + //
            "\t\t\t\"null value\": null" + System.lineSeparator() + //
            "\t\t}" + System.lineSeparator() + //
            "\t]," + System.lineSeparator() + //
            "\t\"config\": {" + System.lineSeparator() + //
            "\t\t\"sub\": {}," + System.lineSeparator() + //
            "\t\t\"string\": \"test\"," + System.lineSeparator() + //
            "\t\t\"null value\": null" + System.lineSeparator() + //
            "\t}," + System.lineSeparator() + //
            "\t\"long\": 123456789," + System.lineSeparator() + //
            "\t\"enum\": \"A\"" + System.lineSeparator() + //
            "}";
    static final String EXPECTED_SERIALIZED_MINIMAL = "{\"bool_array\":[true,false,true,false],\"table_array2\":[{\"sub\":{},\"string\":\"test\",\"null value\":null},{\"sub\":{},\"string\":\"test\",\"null value\":null},{\"sub\":{},\"string\":\"test\",\"null value\":null}],\"string\":\"\\\"value\\\"\",\"double\":3.1415926535,\"integer\":2,\"table_array\":[{\"sub\":{},\"string\":\"test\",\"null value\":null},{\"sub\":{},\"string\":\"test\",\"null value\":null},{\"sub\":{},\"string\":\"test\",\"null value\":null}],\"config\":{\"sub\":{},\"string\":\"test\",\"null value\":null},\"long\":123456789,\"enum\":\"A\"}";
}
