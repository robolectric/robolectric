package org.robolectric.shadows;

import android.graphics.drawable.LayerDrawable;
import org.robolectric.annotation.Implements;

@Implements(LayerDrawable.class)
public class ShadowLayerDrawable extends ShadowDrawable {
}
