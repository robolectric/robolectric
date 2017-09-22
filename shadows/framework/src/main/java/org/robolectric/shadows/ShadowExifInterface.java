package org.robolectric.shadows;

import android.media.ExifInterface;
import org.robolectric.annotation.Implements;

@Implements(value = ExifInterface.class, callThroughByDefault = false)
public class ShadowExifInterface {
}
