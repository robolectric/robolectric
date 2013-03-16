package org.robolectric.shadows;

import android.media.ExifInterface;
import org.robolectric.internal.Implements;

@Implements(value = ExifInterface.class, callThroughByDefault = false)
public class ShadowExifInterface {
}
