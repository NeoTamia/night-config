package re.neotamia.nightconfig.hocon;

/**
 * A style of HOCON comment.
 *
 * @author TheElectronWill
 */
public enum CommentStyle {
	/**
	 * # prefix
	 */
	HASH('#'),
    /**
     * # and space prefix
     */
	HASH_WITH_SPACE('#', ' '),
	/**
	 * // prefix
	 */
	SLASH('/', '/');

	public final char[] chars;

	CommentStyle(char... chars) {
		this.chars = chars;
	}
}
