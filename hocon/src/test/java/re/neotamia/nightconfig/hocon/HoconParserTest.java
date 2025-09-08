package re.neotamia.nightconfig.hocon;

import re.neotamia.nightconfig.core.CommentedConfig;
import re.neotamia.nightconfig.core.InMemoryCommentedFormat;
import re.neotamia.nightconfig.core.concurrent.StampedConfig;
import re.neotamia.nightconfig.core.concurrent.SynchronizedConfig;
import re.neotamia.nightconfig.core.file.FileNotFoundAction;
import re.neotamia.nightconfig.core.io.ParsingMode;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author TheElectronWill
 */
class HoconParserTest {

	@Test
	public void readWriteReadAgain() {
		File file = new File("test.hocon");
		CommentedConfig parsed = new HoconParser().parse(file, FileNotFoundAction.THROW_ERROR);
		Util.checkExample(parsed);

		java.io.StringWriter sw = new StringWriter();
		HoconWriter writer = new HoconWriter();
		writer.write(parsed, sw);

		CommentedConfig reparsed = new HoconParser().parse(new StringReader(sw.toString()));
		Util.checkExample(reparsed);
		assertEquals(parsed, reparsed);
	}

	@Test
	public void readToSynchronizedConfig() {
		File f = new File("test.hocon");
		SynchronizedConfig config = new SynchronizedConfig(InMemoryCommentedFormat.defaultInstance(), HashMap::new);
		new HoconParser().parse(f, config, ParsingMode.REPLACE, FileNotFoundAction.THROW_ERROR);
		Util.checkExample(config);
	}

	@Test
	public void readToStampedConfig() {
		File f = new File("test.hocon");
		StampedConfig config = new StampedConfig(InMemoryCommentedFormat.defaultInstance(), HashMap::new);
		new HoconParser().parse(f, config, ParsingMode.REPLACE, FileNotFoundAction.THROW_ERROR);
		Util.checkExample(config);
	}

}
