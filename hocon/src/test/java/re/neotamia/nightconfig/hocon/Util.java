package re.neotamia.nightconfig.hocon;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Arrays;

import re.neotamia.sharedtests.BasicTestEnum;
import re.neotamia.nightconfig.core.CommentedConfig;
import re.neotamia.nightconfig.core.Config;
import re.neotamia.sharedtests.TestEnum;

public class Util {
    static void checkExample(CommentedConfig config) {
        assertEquals("Comment associated to the boolean array\nWith multiple lines", config.getComment("bool_array"));
        assertEquals(Arrays.asList(true, false, true, false), config.get("bool_array"));

        assertEquals("Comment associated to the string", config.getComment("string"));
        assertEquals("\"value\"", config.get("string"));
        assertEquals(3.1415926535, config.<Double>get("double"));
        assertEquals(2, config.<Integer>get("integer"));

        List<? extends Config> configList = config.get("config_list");
        assertEquals(3, configList.size());
        assertEquals("test", configList.getFirst().get("string"));
        assertTrue(configList.getFirst().<Config>get("sub").isEmpty());

        assertEquals("test", config.get("config.string"));
        assertEquals(123456789, config.getLong("long"));
        assertEquals("test", config.get("nullSub.a"));
        assertNull(config.get("nullSub.n"));
    }

    static void populateTest(CommentedConfig config) {
        Config subConfig = config.createSubConfig();
        subConfig.set("string", "test");
        subConfig.set("enum", BasicTestEnum.C);
        subConfig.set("sub", config.createSubConfig());

        List<Config> configList = Arrays.asList(subConfig, subConfig, subConfig);

        config.set("string", "\"value\"");
        config.set("integer", 2);
        config.set("long", 123456789L);
        config.set("double", 3.1415926535);
        config.set("bool_array", Arrays.asList(true, false, true, false));
        config.set("config", subConfig);
        config.set("config_list", configList);
        config.setComment("string", "Comment 1\nComment 2\nComment 3");
        config.set("enum", TestEnum.A);
    }

    static final String EXPECTED_SERIALIZED =
        "bool_array: [" + System.lineSeparator() + //
        "\ttrue, " + System.lineSeparator() + //
        "\tfalse, " + System.lineSeparator() + //
        "\ttrue, " + System.lineSeparator() + //
        "\tfalse" + System.lineSeparator() + //
        "]" + System.lineSeparator() + //
        "# Comment 1" + System.lineSeparator() + //
        "# Comment 2" + System.lineSeparator() + //
        "# Comment 3" + System.lineSeparator() + //
        "string: \"\\\"value\\\"\"" + System.lineSeparator() + //
        "double: 3.1415926535" + System.lineSeparator() + //
        "integer: 2" + System.lineSeparator() + //
        "config_list: [" + System.lineSeparator() + //
        "\t{" + System.lineSeparator() + //
        "\t\tsub {}" + System.lineSeparator() + //
        "\t\tstring: test" + System.lineSeparator() + //
        "\t\tenum: C" + System.lineSeparator() + //
        "\t}, " + System.lineSeparator() + //
        "\t{" + System.lineSeparator() + //
        "\t\tsub {}" + System.lineSeparator() + //
        "\t\tstring: test" + System.lineSeparator() + //
        "\t\tenum: C" + System.lineSeparator() + //
        "\t}, " + System.lineSeparator() + //
        "\t{" + System.lineSeparator() + //
        "\t\tsub {}" + System.lineSeparator() + //
        "\t\tstring: test" + System.lineSeparator() + //
        "\t\tenum: C" + System.lineSeparator() + //
        "\t}" + System.lineSeparator() + //
        "]" + System.lineSeparator() + //
        "config {" + System.lineSeparator() + //
        "\tsub {}" + System.lineSeparator() + //
        "\tstring: test" + System.lineSeparator() + //
        "\tenum: C" + System.lineSeparator() + //
        "}" + System.lineSeparator() + //
        "long: 123456789" + System.lineSeparator() + //
        "enum: A" + System.lineSeparator();
}
