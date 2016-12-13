package util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    /**
     * Get the current time stamp string.
     *
     * @return Time stamp in string representation.
     */
    public static String getCurrentTimeStamp() {
        return new SimpleDateFormat("[HH:mm:ss.SSS]").format(new Date());
    }
}
