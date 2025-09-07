package com.electronwill.nightconfig.yaml;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import com.electronwill.sharedtests.BasicTestEnum;
import com.electronwill.nightconfig.core.Config;

public class Util {
    static void checkExample(Config config) {
        assertNull(config.get("sub.null"));
        assertNull(config.get("sub.nullObject"));
        assertNull(config.get("null"));

        assertEquals("this is a string", config.get("string"));
        assertEquals("works", config.get(List.of("not.a.subconfig")));
        assertNull(config.get("nullObject"));
        assertEquals(List.of(10, 12), config.get("list"));
        // Handle enum - could be enum object (direct config) or string (after YAML parsing)
        Object enumValue = config.get("enum");
        if (enumValue instanceof String) {
            assertEquals(BasicTestEnum.A, config.getEnum("enum", BasicTestEnum.class));
        } else {
            assertEquals(BasicTestEnum.A, enumValue);
        }

        List<? extends Config> configList = config.get("objectList");
        assertEquals("bar", configList.get(0).get("foo"));
        assertEquals(true, configList.get(1).get("baz"));
    }

    static void populateTest(Config config) {
        config.set("sub.null", null);
        config.set("string", "this is a string");
        config.set("list", List.of(10, 12));
        config.set("enum", BasicTestEnum.A);
        Config sub1 = config.createSubConfig();
        sub1.set("foo", "bar");
        Config sub2 = config.createSubConfig();
        sub2.set("baz", true);
        config.set("objectList", List.of(sub1, sub2));
    }

    static final String EXPECTED_SERIALIZED = """
            sub:
              'null': null
            string: this is a string
            list:
              - '10'
              - '12'
            enum: A
            objectList:
              - foo: bar
              - baz: 'true'
            """;
}
