package re.neotamia.nightconfig.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author TheElectronWill
 */
class AbstractCommentedConfigTest {

	@Test
	public void testClearComments() {
		CommentedConfig config = CommentedConfig.inMemory();
		config.set("a", "a");
		config.setComment("a", "commentA");

		CommentedConfig sub = CommentedConfig.inMemory();
		sub.set("b", "b");
		sub.setComment("b", "commentB");
		config.set("sub", sub);
		config.setComment("sub", "commentSub");

		assertEquals(config.getComment("a"), "commentA");
		assertEquals(config.getComment("sub.b"), "commentB");
		assertEquals(config.getComment("sub"), "commentSub");
		assertEquals(sub.getComment("b"), "commentB");

		for (CommentedConfig.Entry entry : config.entrySet()) {
			assertNotNull(entry.getComment());
		}

		config.clearComments();

		assertNull(config.getComment("a"));
		assertNull(config.getComment("sub.b"));
		assertNull(config.getComment("sub"));
		assertNull(sub.getComment("b"));

		for (CommentedConfig.Entry entry : config.entrySet()) {
			assertNull(entry.getComment());
			assertNotNull(entry.getValue());
		}
	}

    @Test
    public void testHeaderComment() {
        CommentedConfig config = CommentedConfig.inMemory();
        assertNull(config.getHeaderComment());
        assertNull(config.removeHeaderComment());

        config.setHeaderComment("header");
        assertEquals("header", config.getHeaderComment());

        config.setHeaderComment("newHeader");
        assertEquals("newHeader", config.getHeaderComment());

        assertEquals("newHeader", config.removeHeaderComment());
        assertNull(config.getHeaderComment());
    }

}
