package org.robolectric.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Util {
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8196];
        int len;
        try {
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } finally {
            in.close();
        }
    }

    public static byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        copy(inputStream, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static <T> T[] reverse(T[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            int destI = array.length - i - 1;
            T o = array[destI];
            array[destI] = array[i];
            array[i] = o;
        }
        return array;
    }
}
