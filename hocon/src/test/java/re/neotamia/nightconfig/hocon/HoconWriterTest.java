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
		assertEquals(Util.EXPECTED_SERIALIZED, result);
	}

	@Test
	public void writeSynchronizedConfig() {
		CommentedConfig config = new SynchronizedConfig(InMemoryCommentedFormat.defaultInstance(),
				HashMap::new);
		Util.populateTest(config);
		String result = new HoconWriter().writeToString(config);
		assertEquals(Util.EXPECTED_SERIALIZED, result);
	}

	@Test
	public void writeStampedConfig() {
		CommentedConfig config = new StampedConfig(InMemoryCommentedFormat.defaultInstance(),
				HashMap::new);
		Util.populateTest(config);
		String result = new HoconWriter().writeToString(config);
		assertEquals(Util.EXPECTED_SERIALIZED, result);
	}

}
