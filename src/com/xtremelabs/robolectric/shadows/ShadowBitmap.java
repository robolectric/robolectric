package com.xtremelabs.robolectric.shadows;

import android.graphics.Bitmap;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;

import java.io.IOException;
import java.io.OutputStream;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Bitmap.class)
public class ShadowBitmap {
    @RealObject private Bitmap realBitmap;

    @Implementation
    public boolean compress(Bitmap.CompressFormat format, int quality, OutputStream stream) {
        try {
            stream.write(("bitmap compressed as " + format + " with quality " + quality).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}
