package arff.attribute;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enum holding different kind of value types we want to support.
 */
public enum Type {
    NUMERIC, BOOLEAN, SET;

    /**
     * Get a type based on the string representation.
     *
     * @param type String representation of the type.
     * @return String representation of the type.
     */
    public static Type getType(String type) {
        switch (type) {
            case "numeric":
                return NUMERIC;
            case "{0,1}":
                return BOOLEAN;
            default:
                return SET;
        }
    }
}
