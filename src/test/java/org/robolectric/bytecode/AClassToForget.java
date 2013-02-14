package org.robolectric.bytecode;

public class AClassToForget {
    public String memorableMethod() {
        return "get this!";
    }

    public String forgettableMethod() {
        return "shouldn't get this!";
    }

    public static String memorableStaticMethod() {
        return "yess?";
    }

    public static String forgettableStaticMethod() {
        return "noooo!";
    }
}
