package re.neotamia.nightconfig.core.serde;

/**
 * Strategy for transforming field names during serialization and deserialization.
 * <p>
 * This interface defines how Java field names are converted to configuration keys.
 * For example, a field named "userName" might be converted to "user_name" (snake_case)
 * or "user-name" (kebab-case) depending on the strategy used.
 */
@FunctionalInterface
public interface NamingStrategy {

    /**
     * Transforms a field name according to this naming strategy.
     *
     * @param fieldName the original field name from the Java class
     * @return the transformed name to use as a configuration key
     */
    String transformName(String fieldName);

    /**
     * Identity strategy that returns the field name unchanged.
     * This is the default behavior.
     */
    NamingStrategy IDENTITY = fieldName -> fieldName;

    /**
     * Converts field names to snake_case.
     * Example: "userName" -> "user_name"
     */
    NamingStrategy SNAKE_CASE = new SnakeCaseStrategy();

    /**
     * Converts field names to kebab-case.
     * Example: "userName" -> "user-name"
     */
    NamingStrategy KEBAB_CASE = new KebabCaseStrategy();

    /**
     * Converts field names to camelCase.
     * Example: "UserName" -> "userName"
     */
    NamingStrategy CAMEL_CASE = new CamelCaseStrategy();

    /**
     * Converts field names to PascalCase.
     * Example: "userName" -> "UserName"
     */
    NamingStrategy PASCAL_CASE = new PascalCaseStrategy();

    /**
     * Snake case implementation
     */
    final class SnakeCaseStrategy implements NamingStrategy {
        @Override
        public String transformName(String fieldName) {
            if (fieldName == null || fieldName.isEmpty()) return fieldName;

            StringBuilder result = new StringBuilder();
            boolean lastWasUpper = false;

            for (int i = 0; i < fieldName.length(); i++) {
                char c = fieldName.charAt(i);

                if (Character.isUpperCase(c)) {
                    // Add underscore before uppercase letter if it's not the first character
                    // and the previous character wasn't uppercase
                    if (i > 0 && !lastWasUpper) {
                        result.append('_');
                    }
                    result.append(Character.toLowerCase(c));
                    lastWasUpper = true;
                } else {
                    result.append(c);
                    lastWasUpper = false;
                }
            }

            return result.toString();
        }
    }

    /**
     * Kebab case implementation
     */
    final class KebabCaseStrategy implements NamingStrategy {
        @Override
        public String transformName(String fieldName) {
            if (fieldName == null || fieldName.isEmpty()) return fieldName;

            StringBuilder result = new StringBuilder();
            boolean lastWasUpper = false;

            for (int i = 0; i < fieldName.length(); i++) {
                char c = fieldName.charAt(i);

                if (Character.isUpperCase(c)) {
                    // Add hyphen before uppercase letter if it's not the first character
                    // and the previous character wasn't uppercase
                    if (i > 0 && !lastWasUpper) {
                        result.append('-');
                    }
                    result.append(Character.toLowerCase(c));
                    lastWasUpper = true;
                } else {
                    result.append(c);
                    lastWasUpper = false;
                }
            }

            return result.toString();
        }
    }

    /**
     * Camel case implementation
     */
    final class CamelCaseStrategy implements NamingStrategy {
        public static String toCamelCase(String input) {
            if (input == null || input.isEmpty()) {
                return input;
            }

            StringBuilder out = new StringBuilder(input.length());
            boolean firstWord = true;
            boolean upperNext = false;

            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);

                if (!Character.isLetterOrDigit(c)) {
                    upperNext = true;
                    continue;
                }

                if (firstWord) {
                    out.append(Character.toLowerCase(c));
                    firstWord = false;
                } else if (upperNext) {
                    out.append(Character.toUpperCase(c));
                    upperNext = false;
                } else {
                    if (Character.isUpperCase(c)) {
                        boolean nextIsLower = (i + 1 < input.length()) && Character.isLowerCase(input.charAt(i + 1));
                        if (nextIsLower)
                            out.append(Character.toUpperCase(c));
                        else
                            out.append(Character.toLowerCase(c));
                    } else
                        out.append(c);
                }
            }

            return out.toString();
        }

        @Override
        public String transformName(String fieldName) {
            if (fieldName == null || fieldName.isEmpty()) return fieldName;

            return toCamelCase(fieldName);
        }
    }

    /**
     * Pascal case implementation
     */
    final class PascalCaseStrategy implements NamingStrategy {
        @Override
        public String transformName(String fieldName) {
            if (fieldName == null || fieldName.isEmpty()) return fieldName;

            // Convert first character to uppercase
            char firstChar = fieldName.charAt(0);
            if (Character.isLowerCase(firstChar)) {
                return Character.toUpperCase(firstChar) + fieldName.substring(1);
            }

            return fieldName;
        }
    }
}
