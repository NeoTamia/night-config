package re.neotamia.nightconfig.hocon;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import re.neotamia.nightconfig.core.CommentedConfig;
import re.neotamia.nightconfig.core.InMemoryCommentedFormat;
import re.neotamia.nightconfig.core.concurrent.StampedConfig;
import re.neotamia.nightconfig.core.concurrent.SynchronizedConfig;

/**
 * @author TheElectronWill
 */
public class HoconWriterTest {
	@Test
	public void write() throws IOException {
		CommentedConfig config = CommentedConfig.inMemory();
		Util.populateTest(config);

		String result = new HoconWriter().writeToString(config);
		assertEquals("""
                # Comment 1
                # Comment 2
                # Comment 3
                string: "\\"value\\""
                integer: 2
                long: 123456789
                double: 3.1415926535
                bool_array: [
                	true,\s
                	false,\s
                	true,\s
                	false
                ]
                config {
                	string: test
                	enum: C
                	sub {}
                }
                config_list: [
                	{
                		string: test
                		enum: C
                		sub {}
                	},\s
                	{
                		string: test
                		enum: C
                		sub {}
                	},\s
                	{
                		string: test
                		enum: C
                		sub {}
                	}
                ]
                enum: A
                """, result.replaceAll("\r\n", "\n"));
	}

	@Test
	public void writeSynchronizedConfig() {
		CommentedConfig config = new SynchronizedConfig(InMemoryCommentedFormat.defaultInstance(), HashMap::new);
		Util.populateTest(config);
		String result = new HoconWriter().writeToString(config);
		assertEquals(Util.EXPECTED_SERIALIZED, result);
	}

	@Test
	public void writeStampedConfig() {
		CommentedConfig config = new StampedConfig(InMemoryCommentedFormat.defaultInstance(), HashMap::new);
		Util.populateTest(config);
		String result = new HoconWriter().writeToString(config);
		assertEquals(Util.EXPECTED_SERIALIZED, result);
	}

}
