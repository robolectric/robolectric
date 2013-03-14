package org.robolectric.shadows;

import android.widget.Gallery;
import org.robolectric.internal.Implements;

@Implements(value = Gallery.class)
public class ShadowGallery extends ShadowAbsSpinner {
}
