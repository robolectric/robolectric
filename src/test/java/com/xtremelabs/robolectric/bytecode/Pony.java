package com.xtremelabs.robolectric.bytecode;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.Instrument;

@Instrument
public class Pony {
    public Pony() {
    }

    public Pony(String abc) {
        System.out.println("abc = " + abc);
    }

    public String ride(String where) {
        return "Whinny! You're on my " + where + "!";
    }

    public static String prance(String where) {
        return "I'm prancing to " + where + "!";
    }

    public String saunter(String where) {
        return "Off I saunter to " + where + "!";
    }

    @Implements(Pony.class)
    public static class ShadowPony {
        @Implementation
        public String ride(String where) {
            return "Fake whinny! You're on my " + where + "!";
        }

        @Implementation
        public static String prance(String where) {
            return "I'm shadily prancing to " + where + "!";
        }
    }
}
