package re.neotamia.nightconfig.hocon;

import re.neotamia.nightconfig.core.file.CommentedFileConfig;
import java.io.File;
import org.junit.jupiter.api.Test;
import re.neotamia.nightconfig.core.file.FormatDetector;

/**
 * @author TheElectronWill
 */
public class CommentedFileConfigTest {
	public static void main(String[] args) {
		new CommentedFileConfigTest().test();
	}
	@Test
	public void test() {
        FormatDetector.registerExtension("conf", HoconFormat.instance());
        File file = new File("test.conf");
		CommentedFileConfig config = CommentedFileConfig.of(file);
		System.out.println(config.size());
	}
}
