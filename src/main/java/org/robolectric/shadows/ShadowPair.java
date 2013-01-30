package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import android.util.Pair;

import java.lang.reflect.Field;

/**
 * Shadow of {@code Pair}
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Pair.class)
public class ShadowPair {
    @RealObject private Pair realPair;

    public void __constructor__(Object first, Object second) {
        setFields(realPair, first, second);
    }

    @Implementation
    public static <F, S> Pair<F, S> create(F f, S s) {
        return new Pair<F, S>(f, s);
    }

    @Override @Implementation
    public int hashCode() {
        return realPair.first.hashCode() + realPair.second.hashCode();
    }

    @Override @Implementation
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Pair)) return false;
        final Pair other = (Pair) o;
        return realPair.first.equals(other.first) && realPair.second.equals(other.second);
    }

    private static void setFields(Pair p, Object first, Object second) {
        try {
            Field f = Pair.class.getDeclaredField("first");
            f.setAccessible(true);
            f.set(p, first);

            Field s = Pair.class.getDeclaredField("second");
            s.setAccessible(true);
            s.set(p, second);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}


