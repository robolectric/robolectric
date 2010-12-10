package com.xtremelabs.robolectric.util;

import java.util.Collection;

import static org.junit.Assert.assertTrue;

public abstract class TestUtil {
    public static void assertEquals(Collection<?> expected, Collection<?> actual) {
        org.junit.Assert.assertEquals(stringify(expected), stringify(actual));
    }

    public static String stringify(Collection<?> collection) {
        StringBuilder buf = new StringBuilder();
        for (Object o : collection) {
            if (buf.length() > 0) buf.append("\n");
            buf.append(o);
        }
        return buf.toString();
    }

    public static <T> void assertInstanceOf(Class<? extends T> expectedClass, T object) {
        Class actualClass = object.getClass();
        assertTrue(expectedClass + " should be assignable from " + actualClass,
                expectedClass.isAssignableFrom(actualClass));
    }
}
