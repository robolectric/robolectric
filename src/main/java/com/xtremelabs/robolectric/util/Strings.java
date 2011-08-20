package com.xtremelabs.robolectric.util;

import java.io.IOException;
import java.io.InputStream;

public class Strings {

    public static String fromStream(InputStream inputStream) throws IOException {
        int bufSize = 1028;
        byte[] buffer = new byte[bufSize];
        int inSize;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inSize = inputStream.read(buffer)) > 0) {
            stringBuilder.append(new String(buffer, 0, inSize));
        }
        return stringBuilder.toString();
    }

    public static boolean equals(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }
}
