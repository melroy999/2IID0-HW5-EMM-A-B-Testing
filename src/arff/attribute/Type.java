package arff.attribute;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enum holding different kind of value types we want to support.
 */
public enum Type {
    NUMERIC, BOOLEAN, SET, DATE, UUID;

    private static final Pattern pattern = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

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
            //Without {}, as these have been removed previously...
            case "0,1":
                return BOOLEAN;
            default:
                String[] split = type.split(",");
                if(isUUID(split[0])) {
                    return UUID;
                }
                if(isTimeStampValid(split[0])) {
                    return DATE;
                }
                return SET;
        }
    }

    public static boolean isTimeStampValid(String inputString) {
        try {
            DateAttribute.format.parse(inputString);
            return true;
        }
        catch(ParseException e) {
            return false;
        }
    }

    public static boolean isUUID(String inputString) {
        return pattern.matcher(inputString).matches();
    }
}
