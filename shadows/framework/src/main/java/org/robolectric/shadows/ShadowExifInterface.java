package org.robolectric.shadows;

import android.media.ExifInterface;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.media.ExifInterface}.
 */
@Implements(value = ExifInterface.class, callThroughByDefault = false)
public class ShadowExifInterface {
}
