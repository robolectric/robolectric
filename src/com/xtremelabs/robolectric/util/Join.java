package com.xtremelabs.robolectric.util;

import java.util.Collection;

public class Join {
    public static String join(String delimiter, Collection collection) {
        String del = "";
        StringBuilder sb = new StringBuilder();
        for (Object obj : collection) {
            String asString = obj == null ? null : obj.toString();
            if (obj != null && asString.length() > 0) {
                sb.append(del).append(obj);
                del = delimiter;
            }
        }
        return sb.toString();
    }

    public static String join(String delimiter, Object... collection) {
        String del = "";
        StringBuilder sb = new StringBuilder();
        for (Object obj : collection) {
            String asString = obj == null ? null : obj.toString();
            if (asString != null && asString.length() > 0) {
                sb.append(del).append(asString);
                del = delimiter;
            }
        }
        return sb.toString();
    }
}
