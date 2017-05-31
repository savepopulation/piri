package com.raqun;

import java.util.Collection;

/**
 * Created by tyln on 19/05/2017.
 */

public final class ValidationUtil {

    private ValidationUtil() {
        // Private Empty Constructor
    }

    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.size() == 0;
    }

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }
}
