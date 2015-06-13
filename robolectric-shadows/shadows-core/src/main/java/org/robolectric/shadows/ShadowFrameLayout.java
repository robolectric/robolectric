package org.robolectric.shadows;

import android.widget.FrameLayout;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.widget.FrameLayout}.
 */
@SuppressWarnings("UnusedDeclaration")
@Implements(FrameLayout.class)
public class ShadowFrameLayout extends ShadowViewGroup {
}
